package com.gmail.dleemcewen.tandemfieri;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gmail.dleemcewen.tandemfieri.Adapters.OrdersListAdapterAddress;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Enums.OrderEnum;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CompetedOrdersDriverActivity extends AppCompatActivity {
    private User user;
    private String customerId;
    private DatabaseReference mDatabaseDelivery;
    private Context context;
    private ListView ordersListView;
    private List<Order> entities;
    private OrdersListAdapterAddress listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competed_orders_driver);

        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");

        context = this;

        ordersListView = (ListView) findViewById(R.id.orders);
        ordersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                setNotComplete((Order)listAdapter.getItem(i));
                return true;
            }
        });

        LogWriter.log(getApplicationContext(), Level.INFO, "The user is " + user.getAuthUserID());
        mDatabaseDelivery = FirebaseDatabase.getInstance().getReference().child("Delivery").child(user.getAuthUserID()).child("Order");

        mDatabaseDelivery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                entities = new ArrayList<Order>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (DataSnapshot child2 : child.getChildren()) {
                        entities.add(child2.getValue(Order.class));
                    }
                    //Toast.makeText(getApplicationContext(), ""+child.child("Order").getValue(), Toast.LENGTH_LONG).show();
                    //order = child.child("Order").getValue(Order.class);
                }
                loadList();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }//end onCreate

    private void setNotComplete(Order item) {
        final Order temp = item;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        temp.setStatus(OrderEnum.EN_ROUTE);
                        
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
                dialog.dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you wish to mark this order as not complete?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    private void loadList() {
        List<Order> toShow = new ArrayList<>();

        for (Order o : entities) {
            if (o.getStatus() == OrderEnum.COMPLETE)
                toShow.add(o);
        }
        listAdapter = new OrdersListAdapterAddress(context, toShow);
        listAdapter.setUseDate(true);
        ordersListView.setAdapter(listAdapter);
    }
}
