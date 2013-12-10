package io.smartlogic.smartchat;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ContactsAdapter extends CursorAdapter {
    public ContactsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return View.inflate(context, R.layout.adapter_contacts, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

        TextView contactName = (TextView) view.findViewById(R.id.contact_name);
        contactName.setText(cursor.getString(nameFieldColumnIndex));
    }
}
