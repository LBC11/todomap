# todomap
2022 Mobile Programming Term project

### 2022-11-08  
+ Functions  
  + Firebase Authentification  
    + SignUp, SignIn, SignOut, deleteAccount through email&password  
  + Firebase RealtimeBase  
    + For the profile for user and location information  

+ TrableShooting
  + Lack of understanding of setContentView(root)
  
### 2022-11-12  
+ Functions  
  + Firebase Authentification  
    + Social Login using the Google Account  
    
### 2022-11-15  
+ Functions    
  + Connect the Server to MariaDB  
  + CRUD of Todo Entity using the JPA  

+ TrableShooting  
  + custom the query using the JPQL  
  + mariaDB setting using the mySQL Workbench  

### 2022-11-19  
+ Functions  
  + Connect the android app to Server using the Retrofit  
  
+ TrableShooting  
  + When building the server, the file should have been located in the folder containing the application.  
  + Android basically allows only HTTPS, but the initial local address uses HTTP. So, an error occurred.  
    + Solution: Set usesCleartextTraffic to true in Android Manifest.  
  + The Android emulator also uses "http://localhost:8080" by default, so I need to use "http://10.0.2.2:8080/" to connect the Android app to the local server.  
  + The data format returned from server slightly deviated from the json protocol, resulting in Google.gson.stream.malformedjsonexception.  
    + Solution: I used the setLeinet in the Gson builder.  
  
    
