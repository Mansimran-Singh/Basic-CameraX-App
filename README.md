<h1># Skill_Review
Assessment for Code Corp.
</h1>
<h2>
Assignment 1:
Please create an App contains below functions:
</h2>
<li>
Android target SDK = 30 -- Covered
</li> 
<li> 
use Android Camera2 -- Used CameraX library instead which is compatible across devices going back to API level 21 and also provides a Camera2 interop API for implementation with Camera2 code.
</li> 
<li> 
user can choose to open the front camera or rear camera -- Covered
</li> 
<li>   
user can have a preview after take a picture -- Covered
</li> 
<li>  
user can save the picture to storage -- Covered
</li> 
<li>  
user can use a slide bar to adjust zoom level -- Covered (Additional Zoom via up-down keys, FlashMode selection)
</li> 
<h3>
Bonus:
</h3>
<li> 
user can choose to use android Camera1 or android Camera2 -- Covered (As cameraX is backwards compatible)
</li> 
<li> 
user can set resolution for camera preview: 720x1280, 1080x1920, 2160x3840 if device supports it -- Covered (Got the output sizes using Camera2)
</li> 

<h2>
Assignment 2:
Assume you have a picture in the device's storage, please upload the picture to this link  
https://xxx.xxx.xxx/upload/, http method: POST (THIS IS A FAKE URL, PLEASE ASSUME IT WORKS)
</h2>
<li>
Uploaded successfully it returns: response code 200, body:{"message":"success"} -- Covered
</li>
<li>
Failed returns: response code 404, body:{"message":"failed"} -- Covered
</li>
<li>
Please create an App(you can use the same App from Q1): -- Covered
</li>
<li>
use Retrofit or Volley http library to handle request/response -- Covered
</li>
<li>
the progress dialog whiling uploading processing -- Covered
</li>
<li>
use Gson to map the content from server response -- Covered
</li>
