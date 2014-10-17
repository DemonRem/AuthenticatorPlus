AuthenticatorPlus
=================

About
-----
This project is a fork of the Google Authenticator for Android project. This fork adds a **MAJOR SECURITY VULNERABILITY** and should not be used by anyone. I accept no responsibility for compromised account information if you choose to use this app. _You've been warned!_

Using this app will allow you to export the database that contains your OTP secret tokens and import them into the app on another device.

Why?
----
Because I hate resetting all my OTP accounts every time I switch devices. And I don't trust Authy. I'm sure they're nice people, I just don't trust them to store my secret tokens any more securely than I can.

Overview
--------
The Google Authenticator project includes implementations of one-time passcode generators for several mobile platforms, as well as a pluggable authentication module (PAM). One-time passcodes are generated using open standards developed by the Initiative for Open Authentication (OATH) (which is unrelated to OAuth).

These implementations support the HMAC-Based One-time Password (HOTP) algorithm specified in RFC 4226 and the Time-based One-time Password (TOTP) algorithm specified in RFC 6238.

Google Authenticator for Android
--------------------------------
The Android mobile app supports:

* Multiple accounts
* Support for 30-second TOTP codes
* Support for counter-based HOTP codes
* Key provisioning via scanning a QR code
* Manual key entry of RFC 3548 base32 key strings
* DISCLAIMER: This open source project allows you to download the code that powered version 2.21 of the application. Subsequent versions contain Google-specific workflows that are not part of the project.

Credit
------
This source code has been forked from:
* https://code.google.com/p/google-authenticator/
