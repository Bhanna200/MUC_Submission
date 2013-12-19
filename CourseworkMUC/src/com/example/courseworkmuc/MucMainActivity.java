package com.example.courseworkmuc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Locator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;


import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import android.widget.Spinner;

import android.view.View.OnClickListener;

public class MucMainActivity extends FragmentActivity  implements OnItemSelectedListener, OnMarkerClickListener
{
	// map object
	private GoogleMap mMap;
	private LocationManager locMan;	
	private Spinner mapTypeSpinner;
	private Spinner citySpinner;
	private String[] maptypes = {"Normal", "Terrain", "Satelite", "Hybrid"};
	private String[] cityNames ={"Past cities", "Delhi", "Melbourne", "Manchester","kuala Lumpar","Victoria"," Auckland", "Edinburgh","Brisbane","Edmonton", "Christchurch"};
	private int icoUser, icoScotstoun, icoIbrox, icoSecc, icoKelvingrove, icoHockey, icoVeledrome, icoCeltic, icoTollcross, icoHampden, icoCathkin,
				icoStrathclyde, icoEdinburgh, icoDundee, icoCity;	
	private Marker marUser, marScotstoun, marIbrox, marSecc, marKelvingrove, marHockey, marVeledrome, marCeltic, marTollcross, marHampden, marCathkin,
			       marStrathclyde, marEdinburgh, marDundee, marCity;
	private CheckBox chUserLocOnOFF;	
	private List<Marker> markers = new ArrayList<Marker>();
	
	//variables to store default location
	private double defaultLat = 55.85432829452839;
	private double defaultLng = -4.268357989501965;
	
	private final Context context = this;
	
	private MarkerOptions markerOptions;
	private LatLng latLng;
    
    

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muc_main);       
        
     // Getting reference to gotoButton of the layout activity_main
        Button gotoButton = (Button) findViewById(R.id.gotoButton);
        
     // Defining button click event listener for the goto  button
        OnClickListener gotoClickListener = new OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
                // Getting reference to EditText to get the user input location
                EditText enterLocation = (EditText) findViewById(R.id.enterlocation);
 
                // Getting user input location
                String location = enterLocation.getText().toString();
 
                if(location!=null && !location.equals(""))
                {
                    new GeocoderTask().execute(location);
                }
            }
        };
        
        // Setting button click event listener for the find button
        gotoButton.setOnClickListener(gotoClickListener);
     		
        //get drawable IDs
      	icoUser = R.drawable.yellow_point;      	
      	icoScotstoun = R.drawable.squash; 
      	icoIbrox = R.drawable.rugby;
      	icoSecc = R.drawable.boxing;
      	icoKelvingrove = R.drawable.bowls;
      	icoHockey = R.drawable.hockey;
      	icoVeledrome = R.drawable.athetics; 
      	icoCeltic = R.drawable.party;   	 
      	icoTollcross = R.drawable.aqua;     	
      	icoHampden = R.drawable.athetics;      	 
      	icoCathkin = R.drawable.cycleing;
      	icoStrathclyde  = R.drawable.tri; 	 
    	icoEdinburgh  = R.drawable.athetics;  	 
    	icoDundee = R.drawable.shooting;    	
      	icoCity = R.drawable.city;
      	
        //ArrayApapter for apps spinners
        ArrayAdapter <String> adapter = new ArrayAdapter<String>(MucMainActivity.this, R.layout.spinner_layout, maptypes);
        mapTypeSpinner = (Spinner) findViewById(R.id.spinner1);
        mapTypeSpinner.setAdapter(adapter);       
        mapTypeSpinner.setOnItemSelectedListener(this);       
        
        ArrayAdapter <String> cityAdapter = new ArrayAdapter<String>(MucMainActivity.this, R.layout.spinner_layout, cityNames);
        citySpinner = (Spinner) findViewById(R.id.spinner2);
        citySpinner.setAdapter(cityAdapter);       
        citySpinner.setOnItemSelectedListener(this); 

        //checks if the map has been instantiated or not if it hasn't then the map gets istantiated
        if(mMap == null)
        {
        	//passes the map fragment ID from the layout XML and casts it to a map fragment object
        	//and gets the google map object.
        	mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        // the map is drawn then set these default values
        if(mMap != null)
        {
        	defaultSetting();        	
        	addListenerOnChkIos();
        	mMap.setOnMarkerClickListener(this);
        }        
        //showUserLocation();
    } 
    
        
    //An AsyncTask class for accessing the GeoCoding Web Service as this 
    //may become heavy depending on the hardware of the device    
    //code for this method was from 
    //http://wptrafficanalyzer.in/blog/android-geocoding-showing-user-input-location-on-google-map-android-api-v2/
    //and refracterd to this application
 	private class GeocoderTask extends AsyncTask<String, Void, List<Address>>
 	{

 		@Override
 		protected List<Address> doInBackground(String... locationName) 
 		{
 			// Creating an instance of Geocoder class
 			Geocoder geocoder = new Geocoder(getBaseContext());
 			List<Address> addresses = null;
 			
 			try 
 			{
 				// Getting a maximum of 3 Address that matches the input text
 				addresses = geocoder.getFromLocationName(locationName[0], 3);
 			} catch (IOException e) 
 			{
 				e.printStackTrace();
 			}			
 			return addresses;
 		}
 		
 		
 		@Override
 		protected void onPostExecute(List<Address> addresses) 
 		{			
 	        
 	        if(addresses==null || addresses.size()==0)
 	        {
 				Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
 			}
 	        
 	        // Clears all the existing markers on the map
 	        mMap.clear();
 			
 	        // Adding Markers on Google Map for each matching address
 			for(int i=0;i<addresses.size();i++)
 			{				
 				
 				Address address = (Address) addresses.get(i);
 				
 		        // Creating an instance of GeoPoint, to display in Google Map
 		        latLng = new LatLng(address.getLatitude(), address.getLongitude());
 		        
 		        String addressText = String.format("%s, %s",
                         address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                         address.getCountryName());

 		        markerOptions = new MarkerOptions();
 		        markerOptions.position(latLng);
 		        markerOptions.title(addressText);

 		       mMap.addMarker(markerOptions);
 		        
 		        // Locate the first location
 		        if(i==0)
 		        {
 		        	mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); 	
 		        }	       
             
 			}			
 		}		
 	}


    //@Override
   // public boolean onCreateOptionsMenu(Menu menu) 
    //{
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.muc_main, menu);
        //return true;
    //}

 	//depending on what map type the user selects on the spinner this will change the case for the map and set the value of the map
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		// TODO Auto-generated method stub
		int mposition = mapTypeSpinner.getSelectedItemPosition();
		switch(mposition)
		{
		case 0:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			
			break;
			
		case 1:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
			
		case 2:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case 3:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			
			break;
		
		}
		// city names spinner switch case changes the map position to the the value selected by the user and zooms map the  location and sets the latlng to calculate distance.
		int sposition = citySpinner.getSelectedItemPosition();
		switch(sposition)
		{
		case 0:
			//defaultSetting();
			
			break;
		
		case 1:
			LatLng dehli =  new LatLng(28.6100, 77.2300);
			marCity = mMap.addMarker(new MarkerOptions().position(dehli).title("2010 India, New Dehli")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 9" + "\n"  + "Silver: 10" + "\n " + "Bronze: 7"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(dehli), 1000, null);
			latLng  = dehli;
			break;
			
		case 2:
			LatLng Melbourne =  new LatLng(-37.8136, 144.9631);
			marCity = mMap.addMarker(new MarkerOptions().position(Melbourne).title("2006 Austraila, Melbourne")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 11" + "\n"  + "Silver: 7" + "\n" + "Bronze: 11"));
			markers.add(marCity);	
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Melbourne), 1000, null);
			latLng  = Melbourne;
			break;
			
		case 3:
			LatLng Manchester =  new LatLng(53.4667, -2.2333);
			marCity = mMap.addMarker(new MarkerOptions().position(Manchester).title("2002 England, Manchester")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 6" + "\n"  + "Silver: 8" + "\n" + "Bronze: 15"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Manchester), 1000, null);	
			latLng = Manchester;
			break;
		case 4:
			LatLng KualaLumpar =  new LatLng(3.1357, 101.6880);
			marCity = mMap.addMarker(new MarkerOptions().position(KualaLumpar).title("1998 Malaysia, Kuala Lumpur")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 3" + "\n"  + "Silver: 2" + "\n" + "Bronze: 7"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(KualaLumpar), 1000, null);	
			latLng  = KualaLumpar;
			break;
		
		case 5:
			LatLng Victoria =  new LatLng(48.4222, -123.3657);
			marCity = mMap.addMarker(new MarkerOptions().position(Victoria).title("1994 Canada, Victoria")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 6" + "\n"  + "Silver: 3" + "\n" + "Bronze: 11"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Victoria), 1000, null);	
			latLng  = Victoria;
			break;
			
		case 6:
			LatLng Auckland =  new LatLng(-36.8404, 174.7399);
			marCity = mMap.addMarker(new MarkerOptions().position(Auckland).title("1990 New Zealand, Auckland")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 5" + "\n"  + "Silver: 7" + "\n" + "Bronze: 10"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Auckland), 1000, null);	
			latLng  = Auckland;
			break;
			
		case 7:
			LatLng Edinburgh =  new LatLng(55.9531, -3.1889);
			marCity = mMap.addMarker(new MarkerOptions().position(Edinburgh).title("1986, Scotland, Edinburgh")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 3" + "\n"  + "Silver: 12" + "\n" + "Bronze: 18"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Edinburgh), 1000, null);	
			latLng  = Edinburgh;
			break;
			
		case 8:
			LatLng Brisbane =  new LatLng(-27.4679, 153.0278);
			marCity = mMap.addMarker(new MarkerOptions().position(Brisbane).title("1982 Austraila, Brisbane")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 8" + "\n"  + "Silver: 6" + "\n " + "Bronze: 12"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Brisbane), 1000, null);	
			latLng  = Brisbane;
			break;
			
		case 9:
			LatLng Edmonton =  new LatLng(53.5333, -113.5000);
			marCity = mMap.addMarker(new MarkerOptions().position(Edmonton).title("1978 Canada, Edmonton")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 3" + "\n"  + "Silver: 6" + "\n " + "Bronze: 5"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Edmonton), 1000, null);	
			latLng  = Edmonton;
			break;
			
		case 10:
			LatLng Christchurch =  new LatLng(-43.5300, 172.6203);
			marCity = mMap.addMarker(new MarkerOptions().position(Christchurch).title("1974, New Zealand Christchurch")
					.snippet("Medals won by Scotland " + "\n" + "Gold: 3" + "\n"  + "Silver: 5" + "\n " + "Bronze: 11"));
			markers.add(marCity);			
			mMap.animateCamera(CameraUpdateFactory.newLatLng(Christchurch), 1000, null);	
			latLng  = Christchurch;
			break;
		}		
		
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) 
	{
		// TODO Auto-generated method stub
		
	}
	
	private void showUserLocation()
	{
		//get location manager
		locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//get last location
		//Location lastLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location lastLoc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    double lat = lastLoc.getLatitude();
		double lng = lastLoc.getLongitude();
		//create LatLng
		LatLng lastLatLng = new LatLng(lat, lng);

		//remove any existing marker
	    if(marUser!=null) marUser.remove();
		//create and set marker properties
		marUser = mMap.addMarker(new MarkerOptions().position(lastLatLng).title("You are here").icon(BitmapDescriptorFactory.fromResource(icoUser)));
		//move to location
		mMap.animateCamera(CameraUpdateFactory.newLatLng(lastLatLng), 1000, null);				
	}	
	
	public void defaultSetting()
	{		
		//sets the map type
    	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    	// sets the default location of the map to Glasgow
    	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultLat, defaultLng), 12.0f), null );
    	//mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(55.85432829452839, -4.268357989501965)), 3000, null);    	
    	//mMap.addMarker(new MarkerOptions().position(new LatLng(defaultLat,defaultLng)).title("hello"));
    	addMarkers();    	
	}
	
	//adds the defualt markes of the events around glasgow to the map
	public void addMarkers()
	{
		for ( int i = 0; i < 11; i++) 
	    {
			LatLng scotstoun =  new LatLng(55.88069,-4.34025);
			marScotstoun = mMap.addMarker(new MarkerOptions().position(scotstoun).title("Squash and Table Tennis")
					.snippet("SCOTSTOUN SPORTS CAMPUS" + "\n" + "72 Danes Drive" + "\n" + "Glasgow" + "\n" + "G14 9HD").icon(BitmapDescriptorFactory.fromResource(icoScotstoun)));
			markers.add(marScotstoun);
			latLng  = new LatLng(55.88069,-4.34025);			
			
			LatLng ibrox =  new LatLng(55.85360,-4.30454);
			marIbrox = mMap.addMarker(new MarkerOptions().position(ibrox).title("Rugby Sevens")
					.snippet("IBROX STADIUM" + "\n" + "150 Edmiston Drive" + "\n" + "Glasgow" + "\n" + "G51 2XD").icon(BitmapDescriptorFactory.fromResource(icoIbrox)));
			markers.add(marIbrox);
			//latLng  = ibrox;
			
			LatLng secc =  new LatLng(55.86070,-4.28761);
			marSecc = mMap.addMarker(new MarkerOptions().position(secc).title("Boxing, judo, Wrestling")
					.snippet("S.E.C.C" + "\n" + "Exhibition Way" + "\n" + "Glasgow" + "\n" + "G3 8YW").icon(BitmapDescriptorFactory.fromResource(icoSecc)));
			markers.add(marSecc);
			//latLng  = secc;
			
			LatLng kelvingrove =  new LatLng(55.86782,-4.28875);
			marKelvingrove = mMap.addMarker(new MarkerOptions().position(kelvingrove).title("Lawn Bowls")
					.snippet("KELVINGROVE LAWN BOWLS CENTRE" + "\n" + "Kelvin Way" + "\n" + "Glasgow" + "\n" + "G3 7TA ").icon(BitmapDescriptorFactory.fromResource(icoKelvingrove)));
			markers.add(marKelvingrove);
			//latLng  = kelvingrove;
			
			LatLng hockey =  new LatLng(55.84496,-4.23671);
			marHockey = mMap.addMarker(new MarkerOptions().position(hockey).title("Hockey")
					.snippet("GLASGOW NATIONAL HOCKEY CENTRE" + "\n" + "8 King's Drive" + "\n" + "Glasgow" + "\n" + "G40 1HB").icon(BitmapDescriptorFactory.fromResource(icoHockey)));
			markers.add(marHockey);
			//latLng  = hockey;
			
			LatLng veledrome =  new LatLng(55.84496,-4.23671);
			marVeledrome = mMap.addMarker(new MarkerOptions().position(veledrome).title("Athletics")
					.snippet("COMMONWEALTH ARENA & VELODROME" + "\n" + "1000 London Rd" + "\n" + "Glasgow" + "\n" + "G40 3HY").icon(BitmapDescriptorFactory.fromResource(icoVeledrome)));
			markers.add(marVeledrome);
			//latLng  = ibrox;
			
			LatLng celtic =  new LatLng(55.84959,-4.20555);
			marCeltic = mMap.addMarker(new MarkerOptions().position(celtic).title("Opening Ceremony")
					.snippet("CELTIC PARK" + "\n" + "18 Kerrydale St" + "\n" + "Glasgow" + "\n" + "G40 3RE").icon(BitmapDescriptorFactory.fromResource(icoCeltic)));
			markers.add(marCeltic);
			//latLng  = celtic;
			
			LatLng tollcross =  new LatLng(55.84505,-4.17607);
			marTollcross = mMap.addMarker(new MarkerOptions().position(tollcross).title("Aquatics")
					.snippet("TOLLCROSS SWIMMING CENTRE" + "\n" + "67 Wellshot Rd" + "\n" + "Glasgow" + "\n" + "G32 7QP").icon(BitmapDescriptorFactory.fromResource(icoTollcross)));
			markers.add(marTollcross);
			//latLng  = tollcross;
			
			LatLng hampden =  new LatLng(55.82570,-4.25239);
			marHampden = mMap.addMarker(new MarkerOptions().position(hampden).title("Athletics")
					.snippet("HAMPDEN PARK" + "\n" + "Letherby Dr" + "\n" + "Glasgow" + "\n" + "G42 9BA").icon(BitmapDescriptorFactory.fromResource(icoHampden)));
			markers.add(marHampden);
			//latLng  = hampden;
			
			LatLng cathkin =  new LatLng(55.79550,-4.22329);
			marCathkin = mMap.addMarker(new MarkerOptions().position(cathkin).title("Cycling")
					.snippet("CATHKIN BRAES MOUNTAIN BIKE PARK" + "\n" + "Cathkin Road" + "\n" + "Glasgow" + "\n" + "G45").icon(BitmapDescriptorFactory.fromResource(icoCathkin)));
			markers.add(marCathkin);
			//latLng  = cathkin;
			
			LatLng strathclyde =  new LatLng(55.78529,-4.01481);
			marStrathclyde = mMap.addMarker(new MarkerOptions().position(strathclyde).title("Triathlon")
					.snippet("STRATHCLYDE COUNTRY PARK" + "\n" + "366 Hamilton Rd" + "\n" + "Motherwell " + "\n" + "ML1 3ED").icon(BitmapDescriptorFactory.fromResource(icoStrathclyde)));
			markers.add(marStrathclyde);
			//latLng  = strathclyde;
			
			LatLng edinburgh =  new LatLng(55.93920,-3.17273);
			marEdinburgh = mMap.addMarker(new MarkerOptions().position(edinburgh).title("Aquatics")
					.snippet("ROYAL COMMONWEALTH POOL" + "\n" + "21 Dalkeith Rd" + "\n" + "Edinburgh" + "\n" + "EH16 5BB").icon(BitmapDescriptorFactory.fromResource(icoEdinburgh)));
			markers.add(marEdinburgh);
			//latLng  = edinburgh;
			
			LatLng dundee =  new LatLng(56.49302,-2.74663);
			marDundee = mMap.addMarker(new MarkerOptions().position(dundee).title("Shooting")
					.snippet("BARRY BUDDON SHOOTING CENTRE" + "\n" + "Carnoustie" + "\n" + "Angus" + "\n" + "DD7 7RY").icon(BitmapDescriptorFactory.fromResource(icoDundee)));
			markers.add(marDundee);
			//latLng  = dundee;
			//dundeeM = mMap.addMarker(new MarkerOptions().position(new LatLng(56.49302,-2.74663)).title("DUNDEE"));
	    }
		markers.size();
	}
	
	// method to detect when user taps check box
	public void addListenerOnChkIos() 
	{ 
		 
		chUserLocOnOFF = (CheckBox) findViewById(R.id.checkBox);
	 
		chUserLocOnOFF.setOnClickListener(new OnClickListener() 
		{ 
	 
		  @Override
		  public void onClick(View v) 
		  {
	                //is chkIos checked?
			if (((CheckBox) v).isChecked()) 
			{
				showUserLocation(); 
			}
			else				
				defaultSetting();	 
		   }
		  });	 
	 }


	@Override
	public boolean onMarkerClick(Marker marker) 
	{				
		{
			// Create custom dialog object
            final Dialog dialog = new Dialog(MucMainActivity.this);
            // Include dialog.xml file
            dialog.setContentView(R.layout.custom);
            // Set dialog title
            dialog.setTitle(marker.getTitle());

            // set values for custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.textDialog);
            text.setText(marker.getSnippet());
            //ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
            //image.setImageResource(R.drawable.biking);

            dialog.show();
             
            Button backButton = (Button) dialog.findViewById(R.id.backButton);
            Button distanceButton = (Button) dialog.findViewById(R.id.distanceButton);
            // if decline button is clicked, close the custom dialog
            backButton.setOnClickListener(new OnClickListener() 
            {
                @Override
                public void onClick(View v) 
                {
                    // Close dialog
                    dialog.dismiss();
                }
            });
            
            distanceButton.setOnClickListener(new OnClickListener() 
            {
                @Override
                public void onClick(View v) 
                {
                	LatLng glsgowlastLat = new LatLng(defaultLat, defaultLng); 
                    // Close dialog
                	CalculateDistance(glsgowlastLat, latLng);
                }
            });		
		}		
		
		return true;
	}
	
	//method to calculate distance
	public double CalculateDistance(LatLng StartP, LatLng EndP) 
	{
		//radius of earth in Km
        int Radius=6371;
        double OneKilo = 0.6214;
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult= Radius*c;
        double km=valueResult/1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec =  Integer.valueOf(newFormat.format(km));
        int Miles = kmInDec *=  OneKilo;

        //double meter=valueResult%1000;
        //int  meterInDec= Integer.valueOf(newFormat.format(meter));
        //Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);
        Toast.makeText(getBaseContext(), "Distance to the Games:  " + Miles + " Miles", Toast.LENGTH_SHORT).show();
        return Radius * c;
     }



	
	
	
	
}
