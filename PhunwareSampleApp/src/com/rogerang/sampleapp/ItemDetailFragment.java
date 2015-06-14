package com.rogerang.sampleapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rogerang.sampleapp.content.ScheduleItem;
import com.rogerang.sampleapp.content.Venue;
import com.rogerang.sampleapp.content.VenueLoader;

/**
 * A fragment representing a single Venue detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 * 
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The content this fragment is presenting.
     */
    private Venue mItem;

    /**
     * Custom list adapter for ScheduleItems.
     * @author Roger
     *
     */
    public class ScheduleAdapter extends ArrayAdapter<ScheduleItem> {
    	private final LayoutInflater mInflater;    
    	private final SimpleDateFormat dateFormat = new SimpleDateFormat("E M/d");
    	private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma");
    	
    	public ScheduleAdapter(Context context, List<ScheduleItem> venueData) {
    		super(context, R.layout.schedule_list_entry, venueData);
    		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}

    	public View getView(int position, View convertView, ViewGroup parent) {
    		// Inflate a view template
    		if (convertView == null) {
    			convertView = mInflater.inflate(R.layout.schedule_list_entry, parent, false);
    		}
    		TextView tv = (TextView) convertView;    		
    		ScheduleItem item = getItem(position);    		
    		     		
    		Date startDate = item.getStartDate();
    		Date endDate = item.getEndDate();
    		
    		if (startDate != null && endDate != null) {
    			String startDateStr = dateFormat.format(startDate);
    			String schedule =  startDateStr + " " + timeFormat.format(startDate) + " to ";

    			// don't print end date if the same as start date
    			String endDateStr  = dateFormat.format(endDate);
    			if (!endDateStr.equals(startDateStr)) {
    				schedule += endDateStr + " ";
    			}
    			schedule += timeFormat.format(endDate);
    			
    			tv.setText(schedule);            		
    		}
    		return convertView;
    	}
    }
    
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the content specified by the fragment arguments.
            mItem = VenueLoader.ITEM_MAP.get(getArguments().getLong(ARG_ITEM_ID));            
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // Show the content 
        if (mItem != null) {          
        	String imgUrl = mItem.getImageUrl();
   
        	// TODO get and load image
        	
            ((TextView) rootView.findViewById(R.id.venueDetailNameText)).setText(mItem.getName());
            ((TextView) rootView.findViewById(R.id.venueDetailAddressText)).setText(mItem.getAddress());

            // populate schedule list
            List<ScheduleItem> scheduleList = mItem.getSchedule();
            if (scheduleList != null && !scheduleList.isEmpty()) {            	
            	ListView lv = (ListView) rootView.findViewById(R.id.scheduleList);
            	lv.setAdapter(new ScheduleAdapter(getActivity(), scheduleList));
            } 
        }

        return rootView;
    }
    
    public void shareVenue() {
    	if (mItem != null) {
    		String msg = "";
    		
    		if (mItem.getName() != null) {
    			msg += mItem.getName() + ", ";
    		}
    		if (mItem.getAddress() != null) {
    			msg += mItem.getAddress();
    		}    	   	
    		
    		Intent sendIntent = new Intent();
    		sendIntent.setAction(Intent.ACTION_SEND);
    		sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
    		sendIntent.setType("text/plain");
    	    if (sendIntent.resolveActivity(getActivity().getPackageManager()) != null) {
    	        startActivity(sendIntent);
    	    }    		
    	}
    }
}
