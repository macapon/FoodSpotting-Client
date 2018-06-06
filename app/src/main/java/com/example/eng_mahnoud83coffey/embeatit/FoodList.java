package com.example.eng_mahnoud83coffey.embeatit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eng_mahnoud83coffey.embeatit.Interface.ItemClickListener;
import com.example.eng_mahnoud83coffey.embeatit.Model.Food;
import com.example.eng_mahnoud83coffey.embeatit.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    //----------------------------------
    private FirebaseDatabase database;
    private DatabaseReference foodList; //using index
    //----------------------------------
    private RecyclerView recyclerViewFoods;
    private RecyclerView.LayoutManager layoutManager;
    //--------Firebase UI--------//
    private Query query;
    private FirebaseRecyclerOptions<Food> options;
    private FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;
    //--------------------------------------
    private String categoryId;
    //-----------Search Functionality-------
    List<String> suggestList =new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    private FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);


          //-------------------Id----------------------------//
         recyclerViewFoods=(RecyclerView)findViewById(R.id.recyclerView_food);
         materialSearchBar=(MaterialSearchBar)findViewById(R.id.search_Bar_FoodList);//---Search


            //------------------Firebase-------------------------//
        database=FirebaseDatabase.getInstance();
        foodList =database.getReference("Foods");


        //------------------Recycler View------------------//
        recyclerViewFoods.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerViewFoods.setLayoutManager(layoutManager);

        //-----------------Get Intent Here-----------------//
         if (getIntent() != null)
         {
              categoryId=getIntent().getStringExtra("CategoryId");


              if (! categoryId.isEmpty() && categoryId !=null)
              {
                      //Load Data on RecyclerView
                      loadListFoods(categoryId);
              }


         }

         //------------------ Search ----------//
         materialSearchBar.setHint("Enter your Food");
        // materialSearchBar.setSpeechMode(false);//if set to true, microphone icon will be displayed instead of search icon

        loadSuggest();//load suggest from firebase

        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

                //Then user Type their text ,we will change Suggest list

                List<String>suggest=new ArrayList<>();

                for (String search: suggestList) //Loop in suggest List
                {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }

                materialSearchBar.setLastSuggestions(suggest);



            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled)
            {  //When Search Bar is Close
                //Restore Original Adapter

                if (!enabled)
                    recyclerViewFoods.setAdapter(adapter);

            }

            @Override
            public void onSearchConfirmed(CharSequence text)
            { //When Search Finish
              //Show Resulet of Search Adabter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });




    }

    private void startSearch(CharSequence text)
    {

        //---Using Firebase UI to populate a RecyclerView--------//
        query= FirebaseDatabase.getInstance()
                .getReference()
                .child("Foods").orderByChild("Name").equalTo(text.toString()); //Compare Name

        query.keepSynced(true);//Load Data OffLine

        options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);

                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FoodViewHolder holder, final int position, final Food model) {
                // Bind the Chat object to the ChatHolder

                //Send Image Name to Recyclerview
                holder.textFoodName.setText(model.getName());

                //Send Image  to Recyclerview
                Picasso.get()
                        .load(model.getImage())//Url
                        //  .networkPolicy(NetworkPolicy.OFFLINE)//تحميل الصوره Offline
                        //.placeholder(R.drawable.d)//الصوره الافتراضه اللى هتظهر لحد لما الصوره تتحمل
                        .into(holder.foodimageView, new Callback()
                        {
                            @Override
                            public void onSuccess() {//لو الصوره اتحملت offline بنجاح

                            }
                            @Override
                            public void onError(Exception e)
                            {//لو الصوره ماتحملتشى Offline بنجاح اعرضها Online

                                Picasso.get()
                                        .load(model.getImage())
                                        //.placeholder(R.drawable.d)//الصوره الافتراضه اللى هتظهر لحد لما الصوره تتحمل
                                        .into(holder.foodimageView);
                            }
                        });


                final Food clickItem=model;


                //لما المستخدم يضغط على اى صف
                holder.setItemClickListener(new ItemClickListener() {

                    @Override
                    public void onClick(View view, int position, boolean isLongClick)
                    {
                        //Start New Activity

                        Intent foodDeatilsIntent=new Intent(FoodList.this,FoodDetails.class);

                        foodDeatilsIntent.putExtra("FoodId", adapter.getRef(position).getKey());//send food id new Activity
                        startActivity(foodDeatilsIntent);




                    }
                });



            }//end OnBind


        };//end Adapter

        recyclerViewFoods.setAdapter(searchAdapter); //set Adapter for Recycler view is search resulet


    }


    //load suggest from firebase
    //Search
    private void loadSuggest()
    {
         foodList.orderByChild("MenuId").equalTo(categoryId)
                                     .addValueEventListener(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                         {

                                             for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                                             {
                                                        Food foodItem=postSnapshot.getValue(Food.class);

                                                        suggestList.add(foodItem.getName()); //Add name of food to Suggest List
                                             }

                                         }

                                         @Override
                                         public void onCancelled(@NonNull DatabaseError databaseError) {

                                         }
                                     });
    }








     //Load Data from Firebase to displayed Recyclerview
    private void loadListFoods(String categoryId)
    {



        //---Using Firebase UI to populate a RecyclerView--------//
        query= FirebaseDatabase.getInstance()
                .getReference()
                .child("Foods");

        query.keepSynced(true);//Load Data OffLine

        options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query, Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);

                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FoodViewHolder holder, final int position, final Food model) {
                // Bind the Chat object to the ChatHolder

                //Send Image Name to Recyclerview
                holder.textFoodName.setText(model.getName());

                //Send Image  to Recyclerview
                Picasso.get()
                        .load(model.getImage())//Url
                        //  .networkPolicy(NetworkPolicy.OFFLINE)//تحميل الصوره Offline
                        //.placeholder(R.drawable.d)//الصوره الافتراضه اللى هتظهر لحد لما الصوره تتحمل
                        .into(holder.foodimageView, new Callback()
                        {
                            @Override
                            public void onSuccess() {//لو الصوره اتحملت offline بنجاح

                            }
                            @Override
                            public void onError(Exception e)
                            {//لو الصوره ماتحملتشى Offline بنجاح اعرضها Online

                                Picasso.get()
                                        .load(model.getImage())
                                        //.placeholder(R.drawable.d)//الصوره الافتراضه اللى هتظهر لحد لما الصوره تتحمل
                                        .into(holder.foodimageView);
                            }
                        });


                final Food clickItem=model;


                //لما المستخدم يضغط على اى صف
                holder.setItemClickListener(new ItemClickListener() {

                    @Override
                    public void onClick(View view, int position, boolean isLongClick)
                    {
                        //Start New Activity

                        Intent foodDeatilsIntent=new Intent(FoodList.this,FoodDetails.class);

                               foodDeatilsIntent.putExtra("FoodId", adapter.getRef(position).getKey());//send food id new Activity
                               startActivity(foodDeatilsIntent);




                    }
                });



            }//end OnBind


        };//end Adapter

        recyclerViewFoods.setAdapter(adapter);



    }



    //Start Adapter
    @Override
    protected void onStart() {
        super.onStart();

        adapter.startListening();

    }

    //Stop Adapter
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }




}




