package hu.onlineholdem.restclient.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import hu.onlineholdem.restclient.R;

public class DateTimeDialog extends DialogFragment{

    private DatePicker date;
    private TimePicker time;

    public interface DateTimeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    DateTimeDialogListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (DateTimeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DateTimeDialogListener");
        }
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dateTimeView = inflater.inflate(R.layout.date_time_layout, null);
        date = (DatePicker) dateTimeView.findViewById(R.id.datePicker);
        time = (TimePicker) dateTimeView.findViewById(R.id.timePicker);
        time.setIs24HourView(true);

        builder.setView(dateTimeView)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(DateTimeDialog.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DateTimeDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public TimePicker getTime() {
        return time;
    }

    public void setTime(TimePicker time) {
        this.time = time;
    }

    public DatePicker getDate() {
        return date;
    }

    public void setDate(DatePicker date) {
        this.date = date;
    }
}
