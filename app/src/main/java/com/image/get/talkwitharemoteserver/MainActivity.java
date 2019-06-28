package com.image.get.talkwitharemoteserver;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;


public class MainActivity extends AppCompatActivity {


    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private EditText userName;
    private Button finish_user_name;
    private String userNameStr;
    private String prettyName;
    private String imageUrl;
    private String tag;
    private ProgressDialog progress;
    private EditText pretty_name;
    private Button pretty_name_submit;
    private Spinner dropdown;
    private ImageView image;
    private TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        tag = sp.getString("user_tag", "TAG");
        userName = findViewById(R.id.user_name);
        finish_user_name = findViewById(R.id.userName_btn);
        pretty_name = findViewById(R.id.pretty_name);
        pretty_name_submit = findViewById(R.id.pretty_name_btn);
        text = findViewById(R.id.userText);
        dropdown = findViewById(R.id.spinner_img);
        image = findViewById(R.id.profile_image);
        userName.setVisibility(View.INVISIBLE);
        finish_user_name.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        pretty_name.setVisibility(View.INVISIBLE);
        pretty_name_submit.setVisibility(View.INVISIBLE);
        dropdown.setVisibility(View.INVISIBLE);
        image.setVisibility(View.INVISIBLE);

        if (tag.equals("TAG")) {
            setVisible();
            myClickListener();
        }

        do_functions();

    }

    public interface MyServer{

        @GET("/users/{user_name}/token")
        Call<TokenResponse> getUserToken(@Path("user_name") String userName);

        @GET("/user")
        Call<UserResponse> getUserResponse(@Header("Authorization") String token);

        @Headers({
                "Content-Type:application/json"
        })
        @POST("/user/edit/")
        Call<UserResponse> postPrettyName(@Body SetUserPrettyNameRequest request, @Header("Authorization") String token);

        @Headers({
                "Content-Type:application/json"
        })
        @POST("/user/edit/")
        Call<UserResponse> chooseProfileImage(@Body SetUserProfileImageRequest request, @Header("Authorization") String token);

    }

    private void on_failure(Throwable t)
    {
        Toast.makeText(getApplicationContext(), t.getMessage(),
                Toast.LENGTH_LONG).show();
    }



    private void enqueue1(Call<TokenResponse> call, final String new_user)
    {
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (!response.isSuccessful()) {
                    messageToUser2(response);
                } else {
                    dataSet(response, new_user);
                    do_functions();
                }
            }
            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                on_failure(t);
            }
        });
    }

    private void put_str()
    {
        imageUrl = "";
        editor.putString("image_url", imageUrl);
        editor.apply();
    }



    private void enqueue2(Call<UserResponse> call)
    {
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!response.isSuccessful()) {
                    messageToUser22(response);
                    put_str();
                }
                else
                {
                    editor.putString("image_url", imageUrl);
                    editor.apply();
                    image.setVisibility(View.VISIBLE);
                    Picasso.get().load("http://hujipostpc2019.pythonanywhere.com" + imageUrl).into(image);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                on_failure(t);
                put_str();
            }
        });
    }

    private void set_data(Response<UserResponse> response)
    {
        User data = response.body().data;
        prettyName = data.pretty_name;
        imageUrl = data.image_url;
        editor.putString("pretty_name", prettyName);
        editor.putString("image_url", imageUrl);
        editor.apply();
        userNameStr = sp.getString("user_name", "");
        text.setVisibility(View.VISIBLE);
    }

    private void prettyName_cond()
    {
        if (prettyName == null || prettyName.equals("")) {
            text.setText("welcome, " + userNameStr + "!");
        } else {
            text.setText("welcome again, " + prettyName + "!");
        }
    }

    private void enqueue3(Call<UserResponse> call)
    {
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!response.isSuccessful()) {
                    messageToUser22(response);
                } else {
                   set_data(response);
                    prettyName_cond();
                }
            }
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                on_failure(t);
            }
        });
    }


    private void myClickListener()
    {
        finish_user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyServer serverInterface = ServerHolder.getInstance().serverInterface;
                final String new_user = userName.getText().toString();
                if (new_user.equals("")) {
                    messageToUser1();
                } else {
                    Call<TokenResponse> call = serverInterface.getUserToken(new_user);
                    enqueue1(call, new_user);
                }
            }
        });
    }


    public class SetUserPrettyNameRequest {

        public String pretty_name;
    }


    public class SetUserProfileImageRequest {

        public String image_url;
    }

    public class TokenResponse {

        public String data;
    }

    public class User {
        public String pretty_name;
        public String image_url;

    }

    public class UserResponse {
        public User data;
    }


    private void mySelectListener()
    {
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = parent.getItemAtPosition(position).toString();
                imageUrl = "/images/" + selection + ".png";
                MyServer serverInterface = ServerHolder.getInstance().serverInterface;
                SetUserProfileImageRequest request = new SetUserProfileImageRequest();
                request.image_url = imageUrl;
                Call<UserResponse> call = serverInterface.chooseProfileImage(request, "token " + tag);
                enqueue2(call);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void editImageUrl() {
        if (!tag.equals("TAG"))
        {
            dropdown.setVisibility(View.VISIBLE);
            String[] items = new String[]{"crab", "unicorn", "alien", "robot", "octopus", "frog"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
            dropdown.setAdapter(adapter);
            mySelectListener();
        }
    }

    private void visible_view()
    {
        pretty_name.setVisibility(View.VISIBLE);
        pretty_name_submit.setVisibility(View.VISIBLE);
    }

    private void myListen_prettyName()
    {
        pretty_name_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPrettyName = pretty_name.getText().toString();
                if (newPrettyName.equals("")) {
                    messageToUser11();
                }
                else
                {
                    MyServer serverInterface = ServerHolder.getInstance().serverInterface;
                    SetUserPrettyNameRequest request = new SetUserPrettyNameRequest();
                    request.pretty_name = newPrettyName;
                    Call<UserResponse> call = serverInterface.postPrettyName(request, "token " + tag);
                    enqueue3(call);
                }
            }
        });
    }


    private void editPrettyName() {
        if (!tag.equals("TAG")) {
           visible_view();
           myListen_prettyName();

        }
    }




    private void progress_func()
    {
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);
        progress.show();
    }

    private void set_progrss(Response<UserResponse> response)
    {
        progress.dismiss();
        User data = response.body().data;
        prettyName = data.pretty_name;
        imageUrl = data.image_url;
        editor.putString("pretty_name", prettyName);
        editor.putString("image_url", imageUrl);
        editor.apply();
        userNameStr = sp.getString("user_name", "");
        userName.setVisibility(View.GONE);
        finish_user_name.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }

    private void enqueue4(Call<UserResponse> call)
    {
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!response.isSuccessful()) {
                    progress.dismiss();
                    messageToUser22(response);
                } else {
                    set_progrss(response);

                    prettyName_cond();
                }
            }
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progress.dismiss();
                on_failure(t);
            }
        });
    }



    private void getUserDataFromServer() {
        if (!tag.equals("TAG")) {
            MyServer serverInterface = ServerHolder.getInstance().serverInterface;
            progress_func();
            Call<UserResponse> call = serverInterface.getUserResponse("token " + tag);
            enqueue4(call);
        }
    }


    private void setVisible()
    {
        userName.setVisibility(View.VISIBLE);
        finish_user_name.setVisibility(View.VISIBLE);
    }

    private void dataSet(Response<TokenResponse> response, String new_user)
    {
        String data = response.body().data;
        tag = data;
        editor.putString("user_tag", data);
        editor.putString("user_name", new_user);
        editor.apply();
        userName.setVisibility(View.GONE);
        finish_user_name.setVisibility(View.GONE);
    }

    private void do_functions()
    {
        getUserDataFromServer();
        editPrettyName();
        editImageUrl();
    }

    private void messageToUser1()
    {
        Toast.makeText(getApplicationContext(), "Enter a valid username and hit the submit button!",
                Toast.LENGTH_LONG).show();

    }

    private void messageToUser11()
    {
        Toast.makeText(getApplicationContext(), "Enter a valid pretty name and hit the submit button!",
                Toast.LENGTH_LONG).show();
    }

    private void messageToUser22(Response<UserResponse> response)
    {
        Toast.makeText(getApplicationContext(), "code: " + String.valueOf(response.code() + ", try again!"),
                Toast.LENGTH_LONG).show();
    }

    private void messageToUser2(Response<TokenResponse> response)
    {
        Toast.makeText(getApplicationContext(), "code: " + String.valueOf(response.code() + ", try again!"),
                Toast.LENGTH_LONG).show();
    }




}
