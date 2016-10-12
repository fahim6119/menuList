package arefin;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Arefin on 05-Jul-16.
 */

public class SavedEvent
{
    int event_no,itemNum;
    HashMap<String,Integer> paid,total,due;
    String[] descList,servedList;
    int[] priceList;
    public String name,timestamp,place;
    Set<String> users;
    List<Set<String>> menuSet;
    SavedEvent(Context context) {
        initialize(context);
    }
    SavedEvent()
    {

    }

    public void initialize(Context context)
    {
        /*
        dbHelp dbHelper = new dbHelp(context);
        dbWrite= dbHelper.getWritableDatabase();
        dbRead = dbHelper.getReadableDatabase();
        */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> description;
        List<Integer> price;
        ArrayList<String> userList=new ArrayList<>();
        if(preferences.contains("name")) {
            name = preferences.getString("name", null);
            name=name.replace(' ','_') ;
            event_no = preferences.getInt("event_no", 1);
            timestamp = preferences.getString("timestamp", null);
            //String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            place = preferences.getString("place", null);
            /*
            Cursor cursor = dbRead.query("Event_table",
                    new String[]{"_id", "Event_name", "Place", "TimeStamp"},
                    "Event_name = ?",
                    new String[]{name},
                    null, null, null);
            if (cursor == null)
                dbHelper.insertEvent(name, place, timestamp);
                */
        }

        if(preferences.contains("itemNum"))
        {
            itemNum = preferences.getInt("itemNum", 0);

            menuSet = new ArrayList<>(itemNum);

            if(preferences.contains("users"))
            {
                users = preferences.getStringSet("users", null);
                userList = new ArrayList<>(users);
                Collections.sort(userList);
            }

            description=new ArrayList<>(itemNum);
            descList = new String[itemNum];
            for (int i = 0; i < itemNum; i++) {
                descList[i] = preferences.getString("desc_" + i, null);
                description.add(descList[i]);
            }

            price=new ArrayList<>(itemNum);
            priceList = new int[itemNum];
            for (int i = 0; i < itemNum; i++) {
                priceList[i] = preferences.getInt("price_" + i, 0);
                price.add(priceList[i]);
            }

            if(preferences.contains("menu_0"))
            {
                ArrayList<ArrayList<String>> orderer;
                orderer = new ArrayList<>();
                for (int l = 0; l < itemNum; l++) {
                    menuSet.add(preferences.getStringSet("menu_" + l, null));
                    orderer.add(new ArrayList<String>(menuSet.get(l)));
                    Collections.sort(orderer.get(l), String.CASE_INSENSITIVE_ORDER);
                }
                String order_table=name+"_order";
                /*
                dbRead.execSQL("DROP TABLE IF EXISTS "+order_table);
                dbHelper.createOrderTable(dbRead,order_table);
                for(int i=0;i<itemNum;i++)
                {
                    for(int j=0;j<orderer.get(i).size();j++)
                    {
                        String name=orderer.get(i).get(j);
                        dbHelper.insertOrder(order_table,i,name,0,1);
                    }
                }
                */
            }

            servedList= new String[itemNum];
            for(int i=0;i<itemNum;i++)
            {
                servedList[i] = preferences.getString("selected_"+i, null);
            }
            int size=userList.size();
            paid=new HashMap<>(size);
            total=new HashMap<>(size);
            due=new HashMap<>(size);
            for(int i=0;i<size;i++)
            {
                String name=userList.get(i);
                if(preferences.contains("paid_"+name))
                {
                    int valPaid = preferences.getInt("paid_" + name, 0);
                    paid.put(name,valPaid);
                    int valTotal = preferences.getInt("total_" + name, 0);
                    total.put(name,valTotal);
                    int valDue = preferences.getInt("due_" + name, 0);
                    due.put(name,valDue);
                }
            }
            String attendee_table=name+"_attendee";
            /*
            dbRead.execSQL("DROP TABLE IF EXISTS "+attendee_table);
            dbHelper.createAttendeeTable(dbRead,attendee_table);
            for(int i=0;i<userList.size();i++)
            {
                //dbHelper.insertAttendee(attendee_table,userList.get(i),total.get(i),paid.get(i),due.get(i));
            }
            String menu_table=name+"_menu";
            dbRead.execSQL("DROP TABLE IF EXISTS "+menu_table);
            dbHelper.createMenuTable(dbRead,menu_table);
            for(int i=0;i<menuSet.size();i++)
            {
                dbHelper.insertMenu(menu_table,description.get(i),price.get(i));
            }
            dbHelper.close();
            */
        }
    }

    public void writeToPreference(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.putBoolean("backedup",false);
        editor.putInt("event_no",event_no);
        editor.putString("name", name);
        editor.putString("timestamp",new Timestamp(System.currentTimeMillis()).toString());
        editor.putString("place",place);
        editor.putStringSet("users",users );
        ArrayList<String> userList = new ArrayList<>(users);
        editor.putInt("itemNum", itemNum);
        for(int i=0;i<itemNum; i++)
        {
            editor.putInt("price_" + i, priceList[i]);
            editor.putString("desc_" + i, descList[i]);
            editor.putStringSet("menu_" + i, menuSet.get(i));
            editor.putString("selected_"+i,servedList[i]);
        }
        for(int i=0;i<userList.size();i++) {
            String name = userList.get(i);
            int paidAmount = (int) paid.get(name);
            editor.putInt("paid_" + name, paidAmount);
            int totalAmount=(int) total.get(name);
            editor.putInt("total_"+name,totalAmount);
            int dueAmount=(int) due.get(name);
            editor.putInt("due_"+name,dueAmount);
        }
        editor.apply();

    }

    @Override
    public String toString()
    {
        ArrayList<String> userList = new ArrayList<>(users);
        StringBuilder sb=new StringBuilder();
        sb.append("********** Event "+ event_no+"**********\n");
        sb.append("\n");
        sb.append("Event Name : "+name);
        sb.append("\n");
        sb.append("Location : "+place);
        sb.append("\n");
        sb.append("TimeStamp : "+timestamp + "\n");
        sb.append("\n");

        if(users==null)
            sb.append("UserList is Blank\n");
        else {
            sb.append("Attendees : " + users.size());
            sb.append("\n");
            sb.append(users.toString() + "\n");
            sb.append("\n");
        }

        sb.append("Number of Items : "+itemNum+"\n");

        for(int i=0;i<itemNum;i++)
        {
            StringBuilder itemBuilder=new StringBuilder();
            itemBuilder.append("\nItem "+(i+1)+" : "+descList[i]);
            itemBuilder.append(" ; price : "+priceList[i]);
            sb.append(itemBuilder.toString());
            sb.append("\n");
            sb.append("\nOrdered By : "+menuSet.get(i).size());
            sb.append("\n");
            sb.append(menuSet.get(i).toString());
            sb.append("\n");
            String savedString =servedList[i];
            int[] selections;
            ArrayList<String> servedMembers=new ArrayList<String>(menuSet.get(i));
            if(savedString!=null) {
                if (savedString.equals(""))
                    selections = null;
                else {
                    List<String> items = new ArrayList<>(Arrays.asList(savedString.split(",")));
                    selections = new int[items.size()];
                    sb.append("\n Served to : "+selections.length+"\n");
                    for (int j = 0; j < items.size(); j++) {
                        selections[j] = Integer.parseInt(items.get(j));
                    }
                    for(int k=0;k<selections.length;k++)
                        sb.append(servedMembers.get(selections[k])+", ");
                }
            }

            sb.append("\n");
        }

        sb.append("\nPayments Made : \n");
        for(int k=0;k<userList.size();k++)
        {
            String name=userList.get(k);
            //sb.append(name + " "+ paidList[k]+",");
            sb.append(name+"\t :- Bill : "+total.get(name)+", Paid : "+paid.get(name)+" , Due : "+due.get(name)+"\n");
        }
        return sb.toString();
    }
}
