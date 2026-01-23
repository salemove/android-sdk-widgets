# Fragment Migration - Manual Testing Checklist

## Overview
This checklist validates the Fragment-based architecture migration. All existing functionality must work identically to the Activity-based implementation.

**Branch:** `feature/fragment-migration`

## Pre-Testing Setup

### 1. Build and Install
- [ ] Build completed successfully: `./gradlew app:assembleDebug`
- [ ] APK installed on device/emulator
- [ ] LeakCanary enabled in debug build (if available)

### 2. Device Configuration
- [ ] Test device: ____________________ (e.g., Pixel 6, Android 14)
- [ ] Emulator/Physical: ____________________
- [ ] Android version: ____________________

---

## Test Scenarios

### A. ChatActivity / ChatFragment

#### A1. Basic Launch and Display
- [ ] **Intent Launch:** Launch ChatActivity via Intent with `LIVE_CHAT` intention
  - [ ] Fragment loads and displays correctly
  - [ ] ChatView is visible and interactive
  - [ ] Title bar shows correct title
  - [ ] No visual regressions compared to previous version

#### A2. Lifecycle Management
- [ ] **Rotate Device:** Portrait → Landscape → Portrait
  - [ ] Chat content preserved across rotation
  - [ ] No crashes
  - [ ] View state restored correctly
  - [ ] Scroll position maintained
- [ ] **Background/Foreground:** Home button → Return to app
  - [ ] Chat state preserved
  - [ ] Connection maintained
  - [ ] No memory leaks (check LeakCanary)

#### A3. Back Press Handling
- [ ] **Back Button:** Press system back button
  - [ ] ChatView.onBackPressed() called
  - [ ] Proper exit confirmation (if applicable)
  - [ ] Activity finishes correctly

#### A4. Chat Functionality
- [ ] **Send Message:** Type and send text message
  - [ ] Message appears in chat
  - [ ] Input cleared after send
  - [ ] No crashes
- [ ] **Receive Message:** Receive message from operator
  - [ ] Message displays correctly
  - [ ] Timestamp shown
  - [ ] Operator name displayed
- [ ] **Attach File:** Tap attachment button
  - [ ] File picker opens
  - [ ] Selected file attaches
  - [ ] Preview shown
- [ ] **Operator Typing Indicator:** Watch for typing indicator
  - [ ] Indicator appears when operator types
  - [ ] Disappears when operator stops

#### A5. Navigation
- [ ] **Upgrade to Call:** Initiate audio/video call from chat
  - [ ] CallActivity launches correctly
  - [ ] Transition smooth
  - [ ] Chat state preserved
- [ ] **Return to Chat:** Navigate back from call
  - [ ] Chat content intact
  - [ ] No visual glitches

---

### B. CallActivity / CallFragment

#### B1. Basic Launch
- [ ] **Audio Call Launch:** Start audio call engagement
  - [ ] Fragment loads correctly
  - [ ] Audio UI displayed
  - [ ] Microphone permission requested (if needed)
- [ ] **Video Call Launch:** Start video call engagement
  - [ ] Fragment loads correctly
  - [ ] Video UI displayed
  - [ ] Camera permission requested (if needed)
- [ ] **Upgrade to Call:** Upgrade from chat to audio/video
  - [ ] Smooth transition
  - [ ] Call connects
  - [ ] No audio/video issues

#### B2. Lifecycle Management
- [ ] **Rotate Device:** During active call
  - [ ] Video continues
  - [ ] Audio uninterrupted
  - [ ] UI adapts to orientation
  - [ ] No crashes
- [ ] **Background/Foreground:** During call
  - [ ] Call continues in background
  - [ ] Returns to foreground correctly
  - [ ] Video resumes (if video call)

#### B3. Permissions
- [ ] **Camera Permission:** First video call
  - [ ] Permission dialog appears
  - [ ] Grant permission → camera works
  - [ ] Deny permission → appropriate message shown
- [ ] **Microphone Permission:** First audio/video call
  - [ ] Permission dialog appears
  - [ ] Grant permission → mic works
  - [ ] Deny permission → appropriate message shown

#### B4. User Interaction
- [ ] **Mute/Unmute:** Toggle microphone
  - [ ] Mute button visual state changes
  - [ ] Audio mutes/unmutes
  - [ ] Remote side hears correctly
- [ ] **Camera Toggle:** Switch camera (video call)
  - [ ] Front/back camera switches
  - [ ] No black screen
  - [ ] Remote side sees switch
- [ ] **End Call:** Tap end button
  - [ ] Call ends
  - [ ] Fragment finishes
  - [ ] Activity closes

#### B5. Navigation
- [ ] **Navigate to Chat:** From active call
  - [ ] ChatActivity opens
  - [ ] Call continues (or pauses appropriately)
- [ ] **Navigate to WebBrowser:** From call
  - [ ] WebBrowser opens
  - [ ] Call state maintained
- [ ] **Minimize:** Minimize engagement
  - [ ] Returns to previous screen
  - [ ] Call visualizer appears (if enabled)

---

### C. MessageCenterActivity / MessageCenterFragment

#### C1. Basic Launch
- [ ] **Intent Launch:** Open Message Center
  - [ ] Fragment displays correctly
  - [ ] Welcome screen shown (if first time)
  - [ ] Message list loads

#### C2. Activity Result APIs
- [ ] **Attach Photo from Gallery:** Tap attach → Select from gallery
  - [ ] Gallery picker opens
  - [ ] Select image → image attaches
  - [ ] Thumbnail displayed
  - [ ] Can send message with attachment
- [ ] **Attach Document:** Tap attach → Select document
  - [ ] Document picker opens
  - [ ] Document attaches
  - [ ] File name shown
- [ ] **Take Photo:** Tap attach → Take photo
  - [ ] Camera permission requested (if needed)
  - [ ] Camera opens
  - [ ] Take photo → photo attaches
  - [ ] Thumbnail displayed
- [ ] **Multiple Attachments:** Attach multiple files
  - [ ] All attachments shown
  - [ ] Can remove attachments
  - [ ] Send with multiple attachments works

#### C3. Lifecycle Management
- [ ] **Rotate Device:** With attachments pending
  - [ ] Attachments preserved
  - [ ] Message draft preserved
  - [ ] No crashes
- [ ] **Configuration Change:** After taking photo
  - [ ] Photo preserved
  - [ ] Can still send

#### C4. Permissions
- [ ] **Camera Permission:** First photo capture
  - [ ] Permission requested
  - [ ] Grant → camera works
  - [ ] Deny → appropriate message
- [ ] **Storage Permission:** If required on device
  - [ ] Permission requested
  - [ ] Grant → gallery accessible
  - [ ] Deny → handled gracefully

---

### D. ImagePreviewActivity / ImagePreviewFragment

#### D1. Basic Launch
- [ ] **From Chat:** Tap image in chat
  - [ ] ImagePreview opens
  - [ ] Image displays full-screen
  - [ ] Zoom works
  - [ ] Pinch to zoom functional
- [ ] **From Message Center:** Tap attachment
  - [ ] ImagePreview opens
  - [ ] Image displays correctly

#### D2. Options Menu
- [ ] **Save Image:** Tap save menu item
  - [ ] Write permission requested (if needed)
  - [ ] Grant → image saved to gallery
  - [ ] Toast confirms save
  - [ ] Deny → appropriate message
- [ ] **Share Image:** Tap share menu item
  - [ ] Share sheet opens
  - [ ] Select app → image shares correctly

#### D3. Toolbar and Navigation
- [ ] **Toolbar:** Check toolbar display
  - [ ] Title shows image name
  - [ ] Menu items visible
  - [ ] Back button works
- [ ] **System Back:** Press back button
  - [ ] Returns to previous screen
  - [ ] No crashes

#### D4. Lifecycle Management
- [ ] **Rotate Device:** While viewing image
  - [ ] Image reloads
  - [ ] Zoom state reset (expected)
  - [ ] No crashes

---

### E. WebBrowserActivity / WebBrowserFragment

#### E1. Basic Launch
- [ ] **From Chat:** Tap URL link in chat
  - [ ] WebBrowser opens
  - [ ] URL loads
  - [ ] Content displays
- [ ] **From Call:** Navigate to terms/privacy link
  - [ ] WebBrowser opens
  - [ ] URL loads correctly

#### E2. Web Content
- [ ] **Page Loading:** Load complex web page
  - [ ] Progress indicator shown
  - [ ] Page loads completely
  - [ ] JavaScript works (if enabled)
- [ ] **External Links:** Tap external link
  - [ ] Link opens in external browser OR
  - [ ] Handled appropriately based on config

#### E3. Toolbar
- [ ] **Title:** Check title display
  - [ ] Shows page title or custom title
  - [ ] Back button functional

#### E4. Lifecycle Management
- [ ] **Rotate Device:** While page loading
  - [ ] Page continues loading
  - [ ] Content preserved
  - [ ] No crashes

---

### F. SurveyActivity / SurveyFragment

#### F1. Basic Launch
- [ ] **After Engagement:** Complete engagement → survey appears
  - [ ] Survey slides up with animation
  - [ ] Questions displayed
  - [ ] Can interact with survey

#### F2. Touch and Keyboard Handling
- [ ] **Input Field:** Tap text input
  - [ ] Keyboard appears
  - [ ] Can type response
  - [ ] Keyboard doesn't block input
- [ ] **Tap Outside:** Tap outside input field
  - [ ] Keyboard dismisses
  - [ ] Focus removed from input
- [ ] **Tap Outside Survey:** Tap screen area outside survey
  - [ ] Survey dismisses with animation OR
  - [ ] No action (depends on implementation)

#### F3. Survey Interaction
- [ ] **Radio Buttons:** Select rating
  - [ ] Selection visual feedback
  - [ ] Only one selection allowed
- [ ] **Submit:** Complete and submit survey
  - [ ] Validation works
  - [ ] Survey submits successfully
  - [ ] Confirmation shown or activity closes

#### F4. Lifecycle Management
- [ ] **Rotate Device:** With survey open
  - [ ] Survey state preserved
  - [ ] Input preserved
  - [ ] No crashes

---

## Integration Testing

### G. End-to-End Engagement Flows

#### G1. Full Chat Engagement
- [ ] Start chat from entry widget
- [ ] Send messages
- [ ] Receive messages
- [ ] Attach files
- [ ] Rotate device multiple times
- [ ] Background/foreground transitions
- [ ] End engagement
- [ ] Complete survey
- [ ] **No crashes or memory leaks**

#### G2. Full Video Call Engagement
- [ ] Start video call
- [ ] Answer operator call
- [ ] Toggle camera/mic
- [ ] Switch to chat
- [ ] Return to call
- [ ] Rotate device
- [ ] End call
- [ ] Complete survey
- [ ] **No crashes or memory leaks**

#### G3. Message Center Flow
- [ ] Open Message Center
- [ ] Send message with image attachment
- [ ] Send message with document
- [ ] View received message
- [ ] Tap image → preview
- [ ] Background/foreground
- [ ] Rotate device
- [ ] **No crashes or memory leaks**

---

## Memory Leak Testing (LeakCanary)

### H. Memory Leak Checks

For each Activity/Fragment:
- [ ] **ChatFragment:** No leaks after finish
- [ ] **CallFragment:** No leaks after finish
- [ ] **MessageCenterFragment:** No leaks after finish
- [ ] **ImagePreviewFragment:** No leaks after finish
- [ ] **WebBrowserFragment:** No leaks after finish
- [ ] **SurveyFragment:** No leaks after finish

**Memory Leak Detection:**
- [ ] Run full engagement flow
- [ ] Finish all Activities/Fragments
- [ ] Wait for LeakCanary notification
- [ ] **Expected:** No leaks detected
- [ ] **If leaks found:** Document details in "Issues Found" section

---

## Performance Testing

### I. Performance Validation

- [ ] **Launch Time:** Activities launch as quickly as before
- [ ] **Smooth Animations:** All transitions smooth (no jank)
- [ ] **Memory Usage:** Memory usage comparable to previous version
- [ ] **Configuration Changes:** Fast and smooth rotation
- [ ] **Background Return:** Quick resume from background

---

## Regression Testing

### J. Backwards Compatibility

- [ ] **Intent-based Launches:** All existing Intent patterns work
  - [ ] Chat with LIVE_CHAT intention
  - [ ] Call with audio/video MediaType
  - [ ] Message Center launch
  - [ ] WebBrowser with URL
- [ ] **Deep Links:** Deep linking works (if supported)
- [ ] **Push Notifications:** Push notification handling works
- [ ] **Entry Widget:** Entry widget launches correct Activities

---

## Issues Found

### Issue Template
Use this template to document any issues:

**Issue #:** ___
**Component:** (ChatFragment, CallFragment, etc.)
**Description:** ___
**Steps to Reproduce:**
1. ___
2. ___
3. ___

**Expected:** ___
**Actual:** ___
**Severity:** (Critical / High / Medium / Low)
**Screenshots/Logs:** (Attach if applicable)

---

## Test Summary

**Date:** ____________________
**Tester:** ____________________
**Device:** ____________________
**Build:** ____________________

**Test Results:**
- Total Test Cases: ______
- Passed: ______
- Failed: ______
- Blocked: ______

**Overall Status:** ☐ PASS  ☐ FAIL  ☐ PASS WITH ISSUES

**Sign-off:** ____________________

---

## Notes

Add any additional observations, performance notes, or concerns here:

_________________________________________________________________

_________________________________________________________________

_________________________________________________________________
