package com.rogerang.sampleapp.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.util.LongSparseArray;

/**
 * A custom Loader that loads all of the venue data
 */
public class VenueLoader extends AsyncTaskLoader<List<Venue>> {
	List<Venue> mVenues;
	private Context mContext;
	
    public static LongSparseArray<Venue> ITEM_MAP = new LongSparseArray<Venue>();

	public VenueLoader(Context context) {
		super(context);
		mContext = context;
	}
	

    private static List<Venue> readJsonStream(InputStream in) throws IOException, ParseException {
    	JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
    	try {
    		return readVenueArray(reader);
    	} finally {
    		reader.close();
    	}
    }

    private static List<Venue> readVenueArray(JsonReader reader) throws IOException, ParseException {
    	List<Venue> messages = new ArrayList<Venue>();

    	reader.beginArray();
    	while (reader.hasNext()) {
    		messages.add(readVenue(reader));
    	}
    	reader.endArray();
    	return messages;
    }

    private static Venue readVenue(JsonReader reader) throws IOException, ParseException {
    	Venue venue = new Venue();
    	reader.beginObject();
    	while (reader.hasNext()) {
    		String name = reader.nextName();
    		if (name.equals("id")) {
    			venue.setId(reader.nextLong());
    		} else if (name.equals("zip")) {
    			venue.setZip(reader.nextString());
    		} else if (name.equals("phone")) {
    			venue.setPhone(reader.nextString());
    		} else if (name.equals("ticket_link")) {
    			venue.setTicketLink(reader.nextString());
    		} else if (name.equals("state")) {
    			venue.setState(reader.nextString());
    		} else if (name.equals("pcode")) {
    			venue.setPcode(reader.nextInt());
    		} else if (name.equals("city")) {
    			venue.setCity(reader.nextString());
    		} else if (name.equals("tollfreephone")) {
    			venue.setTollFreePhone(reader.nextString());
    		} else if (name.equals("schedule") && reader.peek() != JsonToken.NULL) {
    			venue.setSchedule(readScheduleArray(reader));
    		} else if (name.equals("address")) {
    			venue.setAddress(reader.nextString());              
    		} else if (name.equals("image_url")) {
    			venue.setImageUrl(reader.nextString());              
    		} else if (name.equals("description")) {
    			venue.setDescription(reader.nextString());       
    		} else if (name.equals("name")) {
    			venue.setName(reader.nextString());       
    		} else if (name.equals("longitude")) {
    			venue.setLongitude(reader.nextDouble());      
    		} else if (name.equals("latitude")) {
    			venue.setLatitude(reader.nextDouble());    
    		} else {
    			reader.skipValue();
    		}         
    	}
    	reader.endObject();
    	return venue;
    }
    
    public static List<ScheduleItem> readScheduleArray(JsonReader reader) throws IOException, ParseException {
        List<ScheduleItem> schedules = new ArrayList<ScheduleItem>();

        reader.beginArray();
        while (reader.hasNext()) {
        	schedules.add(readSchedule(reader));
        }
        reader.endArray();
        return schedules;
      }
    
    private static ScheduleItem readSchedule(JsonReader reader) throws IOException, ParseException {
    	String startDate = null;
    	String endDate = null;
    	
    	reader.beginObject();
    	
     	while (reader.hasNext()) {
    		String name = reader.nextName();
    		if (name.equals("end_date")) {
    			endDate = reader.nextString();
    			
    		} else if (name.equals("start_date")) {
    			startDate = reader.nextString();
    		} else {
    			reader.skipValue();
    		}         
    	}
    	
    	reader.endObject();
    	
    	return new ScheduleItem(startDate, endDate);
    }
    			

	/**
	 * This is where the bulk of our work is done.  This function is
	 * called in a background thread and should generate a new set of
	 * data to be published by the loader.
	 */
	@Override public List<Venue> loadInBackground() {
		InputStream inputStream = null;
		List<Venue> newData = null;
		
		try {
			// TEMP load from file to debug JSON parsing
			inputStream = mContext.getAssets().open("test.json");
			newData = readJsonStream(inputStream);

			/* TODO get GSON to work!  Date parsing for ScheduleItem not implemented.                           		
        		InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
        		Type venueType = new TypeToken<List<Venue>>() {}.getType();            
        		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        		ITEMS = gson.fromJson(reader, venueType);
			 */
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) 
					inputStream.close();
			} catch (Exception squish) {
				squish.printStackTrace();
			}
		}

		if (newData == null)
			newData = new ArrayList<Venue>();

		// Done!
		return newData;
	}

	/**
	 * Called when there is new data to deliver to the client.  The
	 * super class will take care of delivering it; the implementation
	 * here just adds a little more logic.
	 */
	@Override public void deliverResult(List<Venue> venues) {
		if (isReset()) {
			// An async query came in while the loader is stopped.  We
			// don't need the result.
			if (venues != null) {
				onReleaseResources(venues);
			}
		}
		List<Venue> oldVenues = mVenues;
		mVenues = venues;
		
		ITEM_MAP.clear();
	   	if (mVenues != null) {
    		for (Venue venue : mVenues) {
    			ITEM_MAP.put(venue.getId(), venue);
    		}
    	}

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(venues);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldVenues != null) {
			onReleaseResources(oldVenues);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override protected void onStartLoading() {
		if (mVenues != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mVenues);
		}


		if (takeContentChanged() || mVenues == null) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override public void onCanceled(List<Venue> venues) {
		super.onCanceled(venues);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(venues);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (mVenues != null) {
			onReleaseResources(mVenues);
			mVenues = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated
	 * with an actively loaded data set.
	 */
	protected void onReleaseResources(List<Venue> venus) {
		// For a simple List<> there is nothing to do.  For something
		// like a Cursor, we would close it here.
	}
}