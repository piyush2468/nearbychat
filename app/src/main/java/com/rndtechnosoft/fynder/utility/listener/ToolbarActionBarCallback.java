package com.rndtechnosoft.fynder.utility.listener;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.adapter.ChatAdapter;
import com.rndtechnosoft.fynder.model.Chat;

import static android.content.Context.CLIPBOARD_SERVICE;


/**
 * Created by Ravi on 3/25/2017.
 */

public abstract class ToolbarActionBarCallback implements ActionMode.Callback{
    private Context context;
    private ChatAdapter adapter;

    public ToolbarActionBarCallback(Context context, ChatAdapter adapter){
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_chat_selected, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy:
                SparseBooleanArray selected = adapter.getSelectedIds();
                int selectedMessageSize = selected.size();

                Log.i("Selected_Items","selectedMessageSize: "+selectedMessageSize);
                //Loop to all selected items
                String message = "";
                for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                    if (selected.valueAt(i)) {
                        Chat model = adapter.getChatList().get(selected.keyAt(i));
                        message += model.getBody()+"\n";
                    }
                }
                Log.w("Selected_Items", "Message : " + message);
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", message);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, context.getString(R.string.copy_message), Toast.LENGTH_SHORT).show();//Show toast
                mode.finish();//Finish action mode
                break;
            case R.id.action_reply:
                onReply(adapter.getChatList().get(adapter.getSelectedIds().keyAt(0)));
                mode.finish();//Finish action mode
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.removeSelection();

    }

    public abstract void onReply(Chat chat);
}
