package com.example.data.local

import org.json.JSONObject

/**
 * Engine handling login form detection, credential extraction, and autofill DOM injection
 * for the Swordfish Android Browser.
 */
object PasswordAutofillEngine {

    /**
     * JavaScript injected into web pages to detect login forms and listen for credentials
     * submission to prompt the user to save passwords securely.
     */
    val LOGIN_DETECTOR_JS = """
        (function() {
            if (window.__swordfish_detector_active) return;
            window.__swordfish_detector_active = true;

            function scanAndNotify() {
                try {
                    var passInputs = document.querySelectorAll('input[type="password"]');
                    if (!passInputs || passInputs.length === 0) return;

                    for (var i = 0; i < passInputs.length; i++) {
                        var passEl = passInputs[i];
                        if (passEl.dataset.swordfishBound) continue;
                        passEl.dataset.swordfishBound = "true";

                        var userEl = null;
                        var form = passEl.closest('form');

                        if (form) {
                            var inputs = form.querySelectorAll('input[type="text"], input[type="email"], input:not([type])');
                            for (var j = 0; j < inputs.length; j++) {
                                var candidate = inputs[j];
                                var attr = ((candidate.name || '') + ' ' + (candidate.id || '') + ' ' + (candidate.autocomplete || '')).toLowerCase();
                                if (attr.match(/(user|login|email|account|identifier|name|member)/)) {
                                    userEl = candidate;
                                    break;
                                }
                            }
                            if (!userEl && inputs.length > 0) {
                                userEl = inputs[inputs.length - 1];
                            }
                        }

                        if (!userEl) {
                            var allInputs = Array.from(document.querySelectorAll('input'));
                            var pIdx = allInputs.indexOf(passEl);
                            for (var k = pIdx - 1; k >= 0; k--) {
                                var prev = allInputs[k];
                                var type = (prev.type || 'text').toLowerCase();
                                if (type === 'text' || type === 'email') {
                                    userEl = prev;
                                    break;
                                }
                            }
                        }

                        var userSel = "";
                        if (userEl) {
                            if (userEl.id) {
                                userSel = '#' + CSS.escape(userEl.id);
                            } else if (userEl.name) {
                                userSel = 'input[name="' + CSS.escape(userEl.name) + '"]';
                            } else {
                                userEl.dataset.swordfishUserTag = "user_" + i;
                                userSel = 'input[data-swordfish-user-tag="user_' + i + '"]';
                            }
                        }

                        var passSel = "";
                        if (passEl.id) {
                            passSel = '#' + CSS.escape(passEl.id);
                        } else if (passEl.name) {
                            passSel = 'input[name="' + CSS.escape(passEl.name) + '"]';
                        } else {
                            passEl.dataset.swordfishPassTag = "pass_" + i;
                            passSel = 'input[data-swordfish-pass-tag="pass_' + i + '"]';
                        }

                        if (window.SwordfishBridge && window.SwordfishBridge.reportLoginFormDetected) {
                            window.SwordfishBridge.reportLoginFormDetected(
                                window.location.hostname,
                                userSel,
                                passSel
                            );
                        }

                        bindSubmitCapture(passEl, userEl);
                    }
                } catch(e) {}
            }

            function bindSubmitCapture(passEl, userEl) {
                function captureAndSend() {
                    try {
                        var pwd = passEl ? passEl.value : '';
                        var usr = userEl ? userEl.value : '';
                        if (pwd && pwd.length >= 2 && usr && usr.length >= 2) {
                            if (window.SwordfishBridge && window.SwordfishBridge.reportPasswordFormSubmitted) {
                                window.SwordfishBridge.reportPasswordFormSubmitted(
                                    window.location.hostname,
                                    usr,
                                    pwd
                                );
                            }
                        }
                    } catch(e) {}
                }

                var form = passEl.closest('form');
                if (form && !form.dataset.swordfishSubmit) {
                    form.dataset.swordfishSubmit = "true";
                    form.addEventListener('submit', function() {
                        captureAndSend();
                    });
                }

                var buttons = document.querySelectorAll('button[type="submit"], input[type="submit"], button, [role="button"]');
                buttons.forEach(function(btn) {
                    var text = (btn.textContent || btn.value || '').toLowerCase();
                    if (text.match(/(log in|sign in|login|signin|submit|continue)/)) {
                        if (!btn.dataset.swordfishClick) {
                            btn.dataset.swordfishClick = "true";
                            btn.addEventListener('click', function() {
                                setTimeout(captureAndSend, 100);
                            });
                        }
                    }
                });
            }

            scanAndNotify();

            window.addEventListener('focusin', function(e) {
                if (e.target && e.target.tagName === 'INPUT') {
                    scanAndNotify();
                }
            });

            var observer = new MutationObserver(function() {
                scanAndNotify();
            });
            if (document.body) {
                observer.observe(document.body, { childList: true, subtree: true });
            }
        })();
    """.trimIndent()

    /**
     * Constructs a safe JavaScript snippet that targets the detected DOM inputs,
     * fills in credentials, and triggers synthetic input & change events.
     */
    fun buildAutofillScript(
        usernameSelector: String,
        passwordSelector: String,
        usernameValue: String,
        passwordValue: String
    ): String {
        val safeUserSelector = JSONObject.quote(usernameSelector)
        val safePassSelector = JSONObject.quote(passwordSelector)
        val safeUserVal = JSONObject.quote(usernameValue)
        val safePassVal = JSONObject.quote(passwordValue)

        return """
            (function() {
                function injectValue(selector, val) {
                    if (!selector || !val) return;
                    try {
                        var el = document.querySelector(selector);
                        if (el) {
                            el.focus();
                            el.value = val;
                            el.dispatchEvent(new Event('input', { bubbles: true, cancelable: true }));
                            el.dispatchEvent(new Event('change', { bubbles: true, cancelable: true }));
                            el.blur();
                        }
                    } catch(e) {
                        console.error('Swordfish autofill error for ' + selector, e);
                    }
                }
                injectValue($safeUserSelector, $safeUserVal);
                injectValue($safePassSelector, $safePassVal);
            })();
        """.trimIndent()
    }
}
