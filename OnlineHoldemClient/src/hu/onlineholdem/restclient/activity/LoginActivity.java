package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.User;
import hu.onlineholdem.restclient.enums.ResponseType;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.PostTask;

public class LoginActivity extends Activity{

    private static final String IP = "80.98.102.139:8080";
    private String newIP;
    private String URL = "http://" + IP  + "/";
    private String SERVICE_URL = "http://" + IP + "/rest";

    private EditText inputUserName;
    private EditText inputPassword;
    private TextView errorMsg;

    private Button btnShowIP;
    private EditText ipText;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        inputUserName = (EditText) findViewById(R.id.textUserName);
        inputPassword = (EditText) findViewById(R.id.textPassword);
        errorMsg = (TextView) findViewById(R.id.errorMsg);
        btnShowIP = (Button) findViewById(R.id.btnShowIP);
        ipText = (EditText) findViewById(R.id.ipText);
        ipText.setText(IP);
        TextView register = (TextView) findViewById(R.id.link_to_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL + "?register"));
                startActivity(browserIntent);
            }
        });
    }

    public void login(View vw) {

        newIP = ipText.getText().toString();
        if(!newIP.equals(IP)){
            URL = "http://" + newIP  + "/";
            SERVICE_URL = "http://" + newIP + "/rest";
        }

        String postURL = SERVICE_URL + "/login";

        PostTask wst = new LoginPostTask(this);

        wst.addNameValuePair("type", "LOGIN");
        wst.addNameValuePair("userName", inputUserName.getText() != null ? inputUserName.getText().toString() : "");
        wst.addNameValuePair("userPassword",  inputPassword.getText() != null ? inputPassword.getText().toString() : "");

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});

    }

    public void showIP(View vw) {
        ipText.setVisibility(View.VISIBLE);

    }
    public void handleLoginResponse(Response response) {

        if(response.getResponseType().equals(ResponseType.OK)){

            Intent gameBrowserActivity = new Intent(getApplicationContext(), GameBrowserActivity.class);
            gameBrowserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            bundle.putLong("userId", ((User) response.getResponseObject()).getUserId());
            bundle.putString("IP", newIP);
            gameBrowserActivity.putExtras(bundle);
            startActivity(gameBrowserActivity);
            finish();
        }
        else{
            errorMsg.setText(response.getErrorMessage());
        }

    }
    public Response parseLoginJson(JSONObject item) throws JSONException {
        Response loginResponse = new Response();

        if (item != null) {
            ResponseType responseType = ResponseType.valueOf(item.getString("responseType"));
            if(responseType.equals(ResponseType.OK)){
                User user = new User();
                JSONObject userJSON = item.getJSONObject("responseObject");
                user.setUserId(userJSON.getLong("userId"));
                user.setUserEmail(userJSON.getString("userEmail"));
                user.setUserName(userJSON.getString("userName"));
                user.setUserPassword(userJSON.getString("userPassword"));
                loginResponse.setResponseObject(user);
                loginResponse.setResponseType(ResponseType.OK);
            }
            if(responseType.equals(ResponseType.ERROR)){
                loginResponse.setResponseType(ResponseType.ERROR);
                loginResponse.setErrorMessage(item.getString("errorMessage"));
            }



        }
        return loginResponse;
    }

    private class LoginPostTask extends PostTask {

        public LoginPostTask(Context mContext) {
            super(mContext);
        }

        @Override
        public void handleResponse(Response response) {
            if(null != response){
                handleLoginResponse(response);
            }
        }

        @Override
        public Response parseJson(JSONObject jsonObject) {
            try {
                return parseLoginJson(jsonObject);
            } catch (JSONException e) {
                Log.e("LoginPostTask", e.getLocalizedMessage(), e);
            }
            return null;
        }
    }
}
