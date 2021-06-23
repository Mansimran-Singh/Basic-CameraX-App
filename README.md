# Skill_Review
Assessment for Code Corp.

Assignment 1:
Please create an App contains below functions:
a. Android target SDK = 30 -- Covered
b. use Android Camera2 -- Used CameraX library instead which is compatible across devices going back to API level 21 and also provides a Camera2 interop API for implementation with Camera2 code.
c. user can choose to open the front camera or rear camera -- Covered
d. user can have a preview after take a picture -- Covered
e. user can save the picture to storage -- Covered
f. user can use a slide bar to adjust zoom level -- Covered (Additional Zoom via up-down keys, FlashMode selection)

Bonus:
a. user can choose to use android Camera1 or android Camera2 -- Covered (As cameraX is backwards compatible)
b. user can set resolution for camera preview: 720x1280, 1080x1920, 2160x3840 if device supports it -- Covered (Got the output sizes using Camera2)


Assignment 2:
Assume you have a picture in the device's storage, please upload the picture to this link -- Covered
https://xxx.xxx.xxx/upload/, http method: POST -- Covered
(THIS IS A FAKE URL, PLEASE ASSUME IT WORKS)
Uploaded successfully it returns: response code 200, body:{"message":"success"} -- Covered
Failed returns: response code 404, body:{"message":"failed"} -- Covered

Please create an App(you can use the same App from Q1): -- Covered
a. use Retrofit or Volley http library to handle request/response -- Covered
b. the progress dialog whiling uploading processing -- Covered
c. use Gson to map the content from server response -- Covered
