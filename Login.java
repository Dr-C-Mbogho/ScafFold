
// March 2017 Version

package com.project.ScafFold;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
 

/*
 * This is the launching activity of the application
 * If the user has not logged in then they will be required to register and/or login and then taken to the main activity
 * If the user is already logged in then they will be directly taken to the main interface
 */

public class Login extends Activity //Login is an activity
{
		final Context context = this; //Use this variable to refer to this activity		
		
	    //Variables for use to fetch Shared preferences		
		public static final String PREFS_NAME = "Prefs"; 
	    private SharedPreferences settings;
	    SharedPreferences.Editor editor;
	   
	    boolean alreadyRegistered; //check if the user is already registered 
	    public static final String TAG = "LOGIN SCREEN LOGS";   //Log Tag
		
		//Declaring component variables
	 	Button btnLogin;
	    Button btnLinkToRegister;
	    EditText inputEmail;
	    EditText inputPassword;
	    TextView ErrorMsg;
	    
	    boolean Advanced; //Used to determine if after login, the user proceeds to the advanced interface
	 
	    // JSON Response node names
	    private static String KEY_SUCCESS = "success";
	    private static String KEY_ERROR = "error";
	    private static String KEY_EMAIL = "email";
	    private static String KEY_CREATED_AT = "created_at";	
	    
	    //Object to access JSon class for fetching JSON data from the PhP files on server
	    JSONObject json; 
	    
	    //Object to access UserFunctions class that contains the methods for JSON Communication
	    UserFunctions userFunction;   
	     
	    //Variables for Time and Date
	    SimpleDateFormat dateFormat;
	  	NumberFormat nf;
	  	short  time;
	  	Timestamp t; 	
	  	String Today; 
	    	
	     
	     @TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	@Override
	
	protected void onCreate(Bundle savedInstanceState) //Called when the activity is created 
	{      
		super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.login); //once the activity is created use the view in login.xml
		
		//Set Title of Screen
		super.setTitle("ScafFold Login"); 
		
		//Save shared preferences to store user login details
		//This prevents user from having to login each time they view the application. Unless they logout
		settings = getSharedPreferences(PREFS_NAME, 0);
		editor = settings.edit();	
	
		//set today's time and date
	 	dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
	 	nf = new DecimalFormat("00");
	 	time = System.currentTimeMillis();	 	
	 	t = new Timestamp(time);
	 	Today = dateFormat.format(t.getTime());
		
		//Check if android API version on the device is greater than 14
	 	//This is checked in order to enable the application to maximize the features of the API that it is installed on
		if (android.os.Build.VERSION.SDK_INT > 10) 
		{
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);  
		  }	
		
		//Check if the user was already logged in, if so proceed to main interface
		//When the user is logged in the settings is saved as "DoneLogged"
		
		//Logged status was committed in Main activity
		
		if (settings.getString("logged", "").toString().equals("Donelogged")) 
		{
			Intent intent = new Intent(Login.this, Main.class);
			startActivity(intent);
		}
		
		// Initialize all components
        inputEmail = (EditText) findViewById(R.id.loginEmail);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);             
        ErrorMsg = (TextView) findViewById(R.id.login_error);
        
        String email = settings.getString("email",""); //Get the email string that was committed in Register class if not yet committed the field will be empty
        inputEmail.setText(email); //Set the email field in login with the user's email address        
        inputPassword.requestFocus();  //Set focus to password field        
      
        //check if the user has not been registered and give relevant message
  		alreadyRegistered = settings.getBoolean("alreadyRegistered", false);  		
  		if(alreadyRegistered == false)
  		{
  			ErrorMsg.setText("If you don't have login details, click on Register Button");
  		}		
  		else
  		{
  			ErrorMsg.setText("");
  		}  		
        
        //Persistent data to track if the advanced interface is on/off
		 Advanced = settings.getBoolean("AdvancedON", false);	 	
      
        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() 
        { 
            public void onClick(View view) 
            {           
               //Check if there is Internet connection. If so proceed.
            	if(CheckInternet(getApplicationContext()))
            	{    
            		String email = inputEmail.getText().toString().trim(); //fetch entered email address
                    String password = inputPassword.getText().toString().trim(); //fetch entered password
                    userFunction = new UserFunctions(); //initialise the object to access UserFunctions class
                   
	                //Check if email field is empty
	                if(email !=null)
		        	{
	                 //Check if password field is empty
		             if(password !=null)
		             { 
		            	 //If both email and password fields are not empty
		            	 
		            	 json = userFunction.loginUser(email, password);		            	 
		            	 
		            	 // check for login response                  
		            	 try 
		            	 {	                	 
	                	  
	                	  if (json.getString(KEY_SUCCESS) != null) 
	                	  {
	                		 
	                        String res = json.getString(KEY_SUCCESS);
	                         
	                        if(Integer.parseInt(res) == 1)
	                        {	                        	
	                        	// user successfully logged in
	                            // Store user details in SQLite Database
	                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
	                            JSONObject json_user = json.getJSONObject("user");
	 
	                            // Clear all previous data in database
	                            userFunction.logoutUser(getApplicationContext());
	                            db.addUser(json_user.getString(KEY_EMAIL), json_user.getString(KEY_CREATED_AT));                        
	                            
	                            // Creating user login session                 
	                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    						SharedPreferences.Editor editor = settings.edit();
	    						
	    						editor.putString("logged", "Justlogged");
	    						editor.commit(); //Commit loginstatus to be used in main interface
	    						
	    						//Commit username to be used in Main	    						
	    						editor.putString("username", email); 
	    						editor.commit();  
	    						
	    						//If the advanced screen had not been arrived at	    						
	    						if(Advanced == false)
	    						{
	    							  // Launch Main Interface Screen
		                            Intent maininterface = new Intent(getApplicationContext(), Main.class);	                           
		                            // Close all views before launching Main Interface
		                            maininterface.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	 
		                            
		                            startActivity(maininterface);
		                            
		                            // Close Login/Register Screen
		                            finish();
	    						}
	    						
	    						
	    						if(Advanced == true)
	    						{
	    							  // Launch Main Interface Screen
		                            Intent maininterface = new Intent(getApplicationContext(), Main2.class);	                           
		                            // Close all views before launching Main Interface
		                            maininterface.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	 
		                            
		                            startActivity(maininterface);
		                            
		                            // Close Login/Register Screen
		                            finish();
	    						}                      	                            
			                	
	                        }
	                	  }
	                	  
	                	     //Check if username does not exist
		                	 if(json.getString(KEY_ERROR) != null)
		                	 {
		                		 String res2 = json.getString(KEY_ERROR); 
		                		
		                		 if(Integer.parseInt(res2) == 1)			                        
			                	 {
		                			 ErrorMsg.setText("User does not exist. Enter correct username and password or Register new user"); 		                			 
		                			 
		                			
		                			 //Make all fields empty after successful registration 
				                	 inputEmail.setText("");
				                	 inputPassword.setText("");            	 
				                	
			                	 }
		                	 }
	                    
	                } catch (JSONException e) 
	                {
	                    e.printStackTrace();
	                }
	                catch (NullPointerException ne)
	                {
	                    ne.printStackTrace();
	                }
	            	
		           }
		            else
		            {
		            	ErrorMsg.setText("Password field empty"); 
		               
		            }
		             	
		             }
			            else
			            {
			            	ErrorMsg.setText("Email field empty"); 	                
	               
			            }
	             	
	            }
            	else
            	{
            		//Display error message if there is no internet connection
            		ErrorMsg.setText("You do not have an internet connection");            		
            		 		
            	}      	
            }
        }); 
        
        // Link to Register 
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() 
        {
 
            public void onClick(View view) 
            {            	
            	Intent i = new Intent(getApplicationContext(), Register.class); 
                startActivity(i);
                finish();            
                
            }
        });  
     
     } 
	     
	     @Override
	     protected void onPause() 
	     {
	         super.onPause();

	         settings = this.getSharedPreferences(PREFS_NAME,Activity.MODE_PRIVATE);			 
	       	editor = settings.edit();      	
	         editor.putString("lastActivity", getClass().getName());
	         editor.commit();
	     }
	    
	     
	  //Method to check if there is wifi or mobile data Internet connection 
	  @SuppressWarnings("deprecation")
	public boolean CheckInternet(Context context) 
	  {
	      ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	      android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	      android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	      if (wifi.isConnected() || mobile.isConnected()) 
	      {
	          return true;
	      } 
	     
	      return false; 
	  }
	  
	 
	  @Override
      public void onStart() 
      {
        super.onStart();
    
        EasyTracker.getInstance().activityStart(this); // Add this method.        
            
      }
	
	  public void onStop() 
	  {		  
		  super.onStop();
		    // The rest of your onStop() code.
		    EasyTracker.getInstance().activityStop(this); // Add this method.
	  }	  
	  
	  public boolean onKeyDown(int keyCode, KeyEvent event)  
	   {  
	   //replaces the default 'Back' button action  
	        if(keyCode==KeyEvent.KEYCODE_BACK)  
	        {  
	           moveTaskToBack(true);
	           return true;  
	        }  
	       return super.onKeyDown(keyCode, event); 
	    }
		
}