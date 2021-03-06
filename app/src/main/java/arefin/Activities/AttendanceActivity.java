package arefin.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.batfia.arefin.ManagerX.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import arefin.Database.Attendee;
import arefin.Database.AttendeeDB;
import arefin.app;
import arefin.dialogs.fragment.ListDialogFragment;
import arefin.dialogs.iface.IMultiChoiceListDialogListener;

public class AttendanceActivity extends AppCompatActivity implements
        IMultiChoiceListDialogListener {
    ListView listView;
    int eventID;
    ArrayList<Attendee> attendeeList;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems;

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    //RECORDING HOW MANY TIMES THE BUTTON HAS BEEN CLICKED
    int clickCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector);
        eventID= app.currentEventID;
        listItems=new ArrayList<String>();
        attendeeList= AttendeeDB.getAttendeesByEvent(eventID);
        for(int i=0;i<attendeeList.size();i++)
        {
            listItems.add(attendeeList.get(i).name);
        }

        listView=(ListView)findViewById(R.id.listView1);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
        clickCounter=0;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text=(String) listView.getItemAtPosition(position);
                EditNameDialog(text,position);
                Log.i("checkLog","Position "+position + " name "+text);
            }
        });
        getSupportActionBar().setTitle("Attendees ( "+listItems.size()+ " )");
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    public void EditNameDialog(String oldName,int position)
    {
        final Dialog dialog = new Dialog(this);
        final int pos=position;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_name_edit, null);
        dialog.setContentView(dialogView);

        dialog.setCancelable(true);

        Button usernameEditButton = (Button) dialogView.findViewById(R.id.usernameEditButton);
        final EditText editUsername = (EditText) dialogView.findViewById(R.id.editUsername);
        editUsername.setHint(oldName);

        final Button usernameCancelButton=(Button) dialogView.findViewById(R.id.buttonEditCancel);
        usernameCancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        usernameEditButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                String userName= editUsername.getText().toString().trim();
                if(TextUtils.isEmpty(userName))
                {
                    Toast.makeText(getBaseContext(), "Invalid Name",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                updateUsername(pos,userName);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void updateUsername(int position,String userName)
    {
        listItems.set(position,userName);
        Attendee attendee=attendeeList.get(position);
        attendee.name=userName;
        AttendeeDB.update(attendee);
        adapter.notifyDataSetChanged();
    }


    public void updateList(String uName)
    {
        listItems.add(uName);
        Attendee attendee=new Attendee(eventID,uName);
        int serial=AttendeeDB.insertAttendee(attendee);
        attendee.serial=serial;
        attendeeList.add(attendee);
        adapter.notifyDataSetChanged();
        getSupportActionBar().setTitle("Attendees ( "+listItems.size()+ " )");
    }

    public void sendNext(View v)
    {

        Bundle b=getIntent().getExtras();
        if(b==null)
        {
            Intent createIntent = new Intent(AttendanceActivity.this, MenuCreatorActivity.class);
            startActivity(createIntent);
            finish();
        }
        else
        {
            Intent createIntent = new Intent(AttendanceActivity.this, FragmentActivity.class);
            startActivity(createIntent);
            finish();
        }

    }

    public void getAttendance()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_attendance, null);
        dialog.setContentView(dialogView);

        dialog.setCancelable(true);

        final Button usernameSetButton = (Button) dialogView.findViewById(R.id.usernameSetButton);
        final EditText newUsername = (EditText) dialogView.findViewById(R.id.newUsername);

        final Button usernameCancelButton=(Button) dialogView.findViewById(R.id.buttonAddCancel);
        usernameCancelButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        usernameSetButton.setOnClickListener(new View.OnClickListener()
        {
             public void onClick(View v) {
                 String userName= newUsername.getText().toString().trim();
                 if(TextUtils.isEmpty(userName))
                 {
                     Toast.makeText(getBaseContext(), "Invalid Name",
                             Toast.LENGTH_SHORT).show();
                     return;
                 }
                 else if(listItems.contains(userName)==true)
                 {
                     newUsername.setError("This person is already attending, please enter a different name if they are two different persons");
                     return;
                 }
                 updateList(userName);
                 dialog.dismiss();
             }
         });
        dialog.show();
    }

    public void sortList()
    {
        Collections.sort(attendeeList, new AttendeeComparator());
        listItems.clear();
        for(int i=0;i<attendeeList.size();i++)
        {
            listItems.add(attendeeList.get(i).name);
        }
        adapter.notifyDataSetChanged();
    }

    public static class AttendeeComparator implements Comparator<Attendee>
    {
        public int compare(Attendee c1, Attendee c2)
        {
            String name1=c1.name.toLowerCase();
            String name2=c2.name.toLowerCase();
            return name1.compareTo(name2);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.collector_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
        }

        else if(id==R.id.action_add_suggestions)
        {
            Intent oldIntent = new Intent(AttendanceActivity.this, SuggestionActivity.class);
            startActivity(oldIntent);
            finish();
        }
        else if (id == R.id.action_add)
        {
            getAttendance();
        }

        else if (id == R.id.action_sort)
        {
            sortList();
        }

        else if (id == R.id.action_remove)
        {
            removeMember();
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeMember()
    {
        String users[]=new String[listItems.size()];
        for (int i = 0; i < listItems.size(); i++) {
            users[i]=listItems.get(i);
        }
        ListDialogFragment frag=new arefin.dialogs.fragment.ListDialogFragment();
            frag.createBuilder(getBaseContext(), getSupportFragmentManager())
                    .setTitle("Remove members from attendee list")
                    .setItems(users)
                    .setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE)
                    .setRequestCode(0)
                    .show();
    }

    @Override
    public void onListItemsSelected(CharSequence[] values, int[] selectedPositions, int choice) {

        int length=selectedPositions.length;
        for(int i=0;i<length;i++)
        {
            listItems.remove(selectedPositions[i]);
            Attendee attendee=attendeeList.get(selectedPositions[i]);
            attendeeList.remove(selectedPositions[i]);
            AttendeeDB.deletebyID(attendee.serial);
        }
        if(length!=0)
            Toast.makeText(getBaseContext(), length+ " Attendee removed", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        getSupportActionBar().setTitle("Attendees ( "+listItems.size()+ " )");
    }

}
