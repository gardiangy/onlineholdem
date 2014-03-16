package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.User;
import hu.onlineholdem.restclient.enums.ResponseType;
import hu.onlineholdem.restclient.response.Response;
import hu.onlineholdem.restclient.task.WebServiceTask;

public class LoginActivity extends Activity{

    private static final String SERVICE_URL = "http://192.168.1.100:8080/rest";

    private EditText inputUserName;
    private EditText inputPassword;
    private TextView errorMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        inputUserName = (EditText) findViewById(R.id.textUserName);
        inputPassword = (EditText) findViewById(R.id.textPassword);
        errorMsg = (TextView) findViewById(R.id.errorMsg);
    }

    public void login(View vw) {

        String postURL = SERVICE_URL + "/login";

        WebServiceTask wst = new LoginTask(WebServiceTask.POST_TASK, this,"Posting data...");

        wst.addNameValuePair("type", "LOGIN");
        wst.addNameValuePair("userName", inputUserName.getText() != null ? inputUserName.getText().toString() : "");
        wst.addNameValuePair("userPassword",  inputPassword.getText() != null ? inputPassword.getText().toString() : "");

        wst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{postURL});

    }
    public void handleLoginResponse(Response response) {

        if(response.getResponseType().equals(ResponseType.OK)){

            Intent gameBrowserActivity = new Intent(getApplicationContext(), GameBrowserActivity.class);
            gameBrowserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            bundle.putLong("userId", ((User) response.getResponseObject()).getUserId());
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

    private class LoginTask extends WebServiceTask {

        public LoginTask(int taskType, Context mContext, String processMessage) {
            super(taskType, mContext, processMessage);
        }

        @Override
        public void handleResponse(Response response) {
            handleLoginResponse(response);
        }

        @Override
        public Response parseJson(JSONObject jsonObject) {
            try {
                return parseLoginJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
