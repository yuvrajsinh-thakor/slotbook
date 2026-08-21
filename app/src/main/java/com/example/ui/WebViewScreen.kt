package com.example.ui

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.automation.SmsReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    configViewModel: ConfigViewModel
) {
    val applicationNumber by configViewModel.applicationNumber.collectAsState()
    val dateOfBirth by configViewModel.dateOfBirth.collectAsState()
    val cov by configViewModel.cov.collectAsState()
    val track by configViewModel.track.collectAsState()
    val targetTimeSlot by configViewModel.targetTime.collectAsState()
    val triggerMs by configViewModel.triggerMs.collectAsState()

    var isArmed by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var statusText by remember { mutableStateOf("Navigate and solve CAPTCHA.") }

    LaunchedEffect(isArmed) {
        if (isArmed) {
            val targetCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, triggerMs.toIntOrNull() ?: 100)
            }
            val targetTimeMillis = targetCalendar.timeInMillis
            
            while (isActive) {
                val now = System.currentTimeMillis()
                if (now >= targetTimeMillis) {
                    statusText = "Triggering Submit!"
                    webViewRef?.evaluateJavascript(
                        "document.getElementById('dlslotipform____SAVE___').click();",
                        null
                    )
                    isArmed = false
                    break
                }
                statusText = "Armed. Waiting for 08:00:00:${triggerMs}..."
                delay(5) // High precision check
            }
        }
    }

    DisposableEffect(Unit) {
        val otpObserver: (String) -> Unit = { otp ->
            webViewRef?.evaluateJavascript(
                "document.getElementById('smsCode').value = '$otp'; document.getElementById('slotcnfrmbtn')?.click();",
                null
            )
        }
        SmsReceiver.onOtpReceived = otpObserver
        onDispose {
            SmsReceiver.onOtpReceived = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { isArmed = !isArmed },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isArmed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isArmed) "Disarm System" else "Arm System (8:00 AM)")
            }
            Text(statusText, modifier = Modifier.padding(8.dp))
        }

        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            
                            val js = """
                                (function() {
                                    function forceFill(selector, value) {
                                        const el = document.querySelector(selector);
                                        if (el && el.value !== value) {
                                            el.value = value;
                                            const events = ['input', 'change', 'blur'];
                                            events.forEach(evt => el.dispatchEvent(new Event(evt, { bubbles: true })));
                                        }
                                    }
                                    
                                    setInterval(() => {
                                        // Auto-detect 503 Service Unavailable and refresh
                                        const pageText = document.body ? document.body.innerText : "";
                                        const pageTitle = document.title || "";
                                        if (pageTitle.includes("503") || pageTitle.includes("Service Unavailable") || pageText.includes("503 Service Unavailable")) {
                                            window.location.reload();
                                            return;
                                        }
                                        
                                        // Auto-fill Page 1
                                        if (document.getElementById('applno')) {
                                            forceFill("#applno", "$applicationNumber");
                                            forceFill("#dob", "$dateOfBirth");
                                            const radio = document.querySelector("#dlslotipform_subtype1");
                                            if (radio && !radio.checked) radio.click();
                                        }

                                        // Auto-fill Page 2 (Track & COV)
                                        if (document.getElementById('trackName')) {
                                            const track = document.getElementById('trackName');
                                            if (track.value !== "$track") {
                                                track.value = "$track";
                                                track.dispatchEvent(new Event('change', { bubbles: true }));
                                            }
                                            const checkbox = document.querySelector('input.chk[value="$cov"]');
                                            if (checkbox && !checkbox.checked) checkbox.click();
                                            
                                            const proceedBtn = document.getElementById('prcdbook');
                                            if (proceedBtn && !proceedBtn.disabled) proceedBtn.click();
                                        }

                                        // Auto-fill Page 3 (Calendar & Time Slot)
                                        const dateSlot = document.querySelector("td.cal_green a");
                                        if (dateSlot) dateSlot.click();
                                        
                                        const timeRadios = document.querySelectorAll("input[type='radio']:not([disabled])");
                                        if (timeRadios.length > 0) {
                                            let selected = false;
                                            for (let radio of timeRadios) {
                                                const rowText = radio.closest('tr').innerText;
                                                if (rowText.includes("$targetTimeSlot") && !radio.checked) {
                                                    radio.click();
                                                    if (typeof window.covSelection === "function") window.covSelection(radio.id);
                                                    selected = true;
                                                    break;
                                                }
                                            }
                                            if (!selected && !timeRadios[0].checked) {
                                                timeRadios[0].click();
                                                if (typeof window.covSelection === "function") window.covSelection(timeRadios[0].id);
                                            }
                                            const bookBtn = document.getElementById("slotbtn");
                                            if (bookBtn && !bookBtn.disabled) bookBtn.click();
                                        }
                                        
                                        // Auto-confirm Popup
                                        const confirmYes = document.getElementById("btnYes");
                                        if (confirmYes && confirmYes.offsetParent !== null) confirmYes.click();
                                        
                                    }, 100);
                                })();
                            """.trimIndent()
                            
                            view?.evaluateJavascript(js, null)
                        }
                    }
                    loadUrl("https://sarathi.parivahan.gov.in/sarathiservice/stateSelection.do")
                    webViewRef = this
                }
            }
        )
    }
}
