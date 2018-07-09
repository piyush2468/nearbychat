package com.rndtechnosoft.fynder.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rndtechnosoft.fynder.FynderApplication;
import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.activity.NearbyHomeActivity;
import com.rndtechnosoft.fynder.adapter.ChatAdapter;
import com.rndtechnosoft.fynder.dialog.ReplyMessageInputView;
import com.rndtechnosoft.fynder.model.Chat;
import com.rndtechnosoft.fynder.utility.Constants;
import com.rndtechnosoft.fynder.utility.MyDateUtil;
import com.rndtechnosoft.fynder.utility.listener.RecyclerTouchListener;
import com.rndtechnosoft.fynder.utility.listener.SendGlobalChatListener;
import com.rndtechnosoft.fynder.utility.listener.ToolbarActionBarCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravi on 11/11/2016.
 */

public class GlobalFragment extends Fragment implements SendGlobalChatListener {
    private final String TAG = "GlobalRoom";
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayout progressBarView;
    private ActionMode mActionMode;
    private RecyclerTouchListener recyclerTouchListener;
    private ChatAdapter adapter;
    private List<Chat> list = new ArrayList<>();
    private DatabaseReference ref;
    private boolean isLastItemHidden = false;
    private int countBadge = 0;
    public static MenuItem item;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.i(TAG, "On Create");
        ref = FirebaseDatabase.getInstance().getReference("rooms").child("global").child("message");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "On CreateView");

        View rootView = inflater.inflate(R.layout.fragment_global_chat, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list_global_chat);
        progressBarView = (LinearLayout) rootView.findViewById(R.id.progressBarView);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Log.i(TAG, "User id: " + firebaseUser.getUid());
            adapter = new ChatAdapter(getActivity(), list, firebaseUser.getUid());
            recyclerView.setAdapter(adapter);
            Log.i(TAG, "Adapter count: " + adapter.getItemCount());
        }
        recyclerTouchListener = new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.RecyclerOnClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(mActionMode == null){
                    Log.e(TAG,"Action mode is NULL");
                    return;
                }
                Log.i(TAG,"Count selected: "+adapter.getSelectedCount());
                if(adapter.getSelectedCount() > 0){
                    adapter.toggleSelection(position);
                    mActionMode.setTitle(String.valueOf(adapter.getSelectedCount()));
                }

                if(adapter.getSelectedCount() == 0){
                    mActionMode.finish();
                }else{
                    Menu menu = mActionMode.getMenu();
                    menu.findItem(R.id.action_reply).setVisible(adapter.getSelectedCount() == 1);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.i(TAG,"OnLongClick on message: "+list.get(position).getBody());
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ToolbarActionBarCallback(getActivity(), adapter) {
                    @Override
                    public void onReply(Chat chat) {
                        Log.i(TAG,"Reply => Message: "+chat.getBody());
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        ((NearbyHomeActivity) getActivity()).getInputTextChat().requestFocus();

                        ((NearbyHomeActivity) getActivity()).setReplyMessageId(chat.getId());
                        ((NearbyHomeActivity) getActivity()).getInputReplyLayout().addView(new ReplyMessageInputView(((NearbyHomeActivity)getActivity()), chat,
                                Color.parseColor(adapter.getColor(chat.getFrom()))));
                    }
                });
                adapter.toggleSelection(position);
                if(adapter.getSelectedCount() > 0){
                    mActionMode.setTitle(String.valueOf(adapter.getSelectedCount()));
                }
                if(adapter.getSelectedCount() == 0){
                    mActionMode.finish();
                }else{
                    Menu menu = mActionMode.getMenu();
                    menu.findItem(R.id.action_reply).setVisible(adapter.getSelectedCount() == 1);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "On Start");
        super.onStart();
        list.clear();
        FynderApplication.getInstance().sendScreenviwedevent(Constants.CT_GLOBAL,"");
        if(recyclerTouchListener != null) {
            recyclerView.addOnItemTouchListener(recyclerTouchListener);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        ref.addChildEventListener(childEventListener);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBarView.setVisibility(View.GONE);
                ref.removeEventListener(this);
                isLastItemHidden = false;
                Log.w(TAG,"End reading...");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void onStop() {
        Log.i(TAG, "On Stop");
        super.onStop();
        isLastItemHidden = false;
        if(recyclerTouchListener != null) {
            recyclerView.removeOnItemTouchListener(recyclerTouchListener);
        }
        ref.removeEventListener(childEventListener);
        for (Chat model : list) {
            model.detachProfileRef();
        }
        if(mRunnable != null){
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
        recyclerView.removeOnScrollListener(onScrollListener);
        hideUnreadMessage();
//        mHandler.removeCallbacks(showButtonDown);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        Log.i(TAG, "On Create option menu");
        inflater.inflate(R.menu.menu_gloabal, menu);
        refresh(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    public void refresh(Menu menu) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView imageView = (ImageView) inflater.inflate(R.layout.action_refresh, null);
        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        imageView.startAnimation(rotation);
        item = menu.findItem(R.id.action_online);
        item.setActionView(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = ((NearbyHomeActivity) getActivity()).getDrawer();
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });
    }

    public static void completeRefresh(MenuItem item) {
        try {
            if(item!=null) {
                item.getActionView().clearAnimation();
                item.setActionView(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_online) {
            FynderApplication.getInstance().sendActionevent(Constants.CT_GLOBAL_ONLINE);
            DrawerLayout drawerLayout = ((NearbyHomeActivity) getActivity()).getDrawer();
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Chat chat = dataSnapshot.getValue(Chat.class);
            chat.setId(dataSnapshot.getKey());
            chat.setReplyChat(findChatById(chat.getReplyMessage()));
            Log.i(TAG, "Message: " + chat.getBody() + " , is last item hidden: " + isLastItemHidden);
//            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//            if(chat.getType() == Chat.TYPE_IMAGE && chat.getUrl() == null && firebaseUser != null
//                    && !chat.getFrom().equals(firebaseUser.getUid())){
//                Log.e(TAG,"The image is not contain the image Url ");
//                return;
//            }
            String thisDate = MyDateUtil.getDateMessage(getActivity(), chat.getTimestamp());
            if (list.size() == 0 || (list.size() > 1 && list.get(list.size() - 1).getTimestamp() > 0 &&
                    !thisDate.equals(MyDateUtil.getDateMessage(getActivity(), list.get(list.size() - 1).getTimestamp())))) {
                //this is for date message only
                Chat dateMessage = new Chat();
                dateMessage.setType(Chat.TYPE_DATE);
                dateMessage.setBody(thisDate);
                list.add(dateMessage);
            }
            if (list.size() > 1 && list.get(list.size() - 1).getTimestamp() > 0 && chat.getType() == Chat.TYPE_TEXT) {
                Chat prevModel = list.get(list.size() - 1);
//                long diff = chat.getTimestamp() - prevModel.getTimestamp();
//                if (prevModel.getFrom().equals(chat.getFrom())
//                        && MyDateUtil.isMergeRequired(chat.getTimestamp(), prevModel.getTimestamp())
//                        && prevModel.getType() == Chat.TYPE_TEXT
//                        && chat.getType() == Chat.TYPE_TEXT && !isLastItemHidden) {
////                    Log.i(TAG, "Diff time: " + diff + " , message: " + chat.getBody());
//                    String newBody = prevModel.getBody() + "\n" + chat.getBody();
//                    prevModel.setBody(newBody);
//                    prevModel.setTimestamp(chat.getTimestamp());
//                    if (adapter != null) {
//                        adapter.notifyItemChanged(list.size() - 1);
//                    }
//                    int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                    if (viewPosition >= list.size() - 2) {
//                        Log.i(TAG, "scroll to bottom");
//                        scrollToBottom();
//                    }
//                } else {
                    addMessage(chat);
//                }
            } else {
                addMessage(chat);
            }
            //check last position is NOT shown
            if (isLastItemHidden) {
                countBadge++;
                ((NearbyHomeActivity) getActivity()).showDownNotification(countBadge);
                //BEGIN add unread message
                boolean isUnreadExist = false;
                for (int i = list.size() - 1; i >= 0; i--) {
                    Chat model = list.get(i);
                    if (model.getType() == Chat.TYPE_UNREAD_MESSAGE) {
                        isUnreadExist = true;
                        String message = String.format(getString(R.string.unread_message), countBadge);
                        model.setBody(message);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
                if (!isUnreadExist) {
                    Chat unRead = new Chat();
                    unRead.setType(Chat.TYPE_UNREAD_MESSAGE);
                    String message = countBadge == 1 ? getString(R.string.unread_message_only_one) :
                            String.format(getString(R.string.unread_message), countBadge);
                    unRead.setBody(message);
                    list.add(list.size() - 1, unRead);
                }
                Log.i(TAG, "Is unread message exist: " + isUnreadExist);
                //END add unread message
            } else {
                hideUnreadMessage();
            }

//            mHandler.postDelayed(showButtonDown, 100);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Chat chat = dataSnapshot.getValue(Chat.class);
            chat.setId(dataSnapshot.getKey());
            Log.i(TAG,"On Child changed..");
//            boolean isExist = false;
//            for (Chat model : list) {
//                if(model.getId().equals(chat.getId())){
//                    isExist = true;
//                    break;
//                }
//            }
//            if(!isExist){
//                addMessage(chat);
//            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            for (int i = list.size() - 1; i >= 0; i--) {
                Chat model = list.get(i);
                Log.i(TAG,"Remove, id: "+model.getId()+" , body: "+model.getBody());
                if(model.getId() == null){
                    list.remove(i);
                    adapter.notifyItemRemoved(i);
                    continue;
                }
                if (model.getId() != null && model.getId().equals(dataSnapshot.getKey())
                        && i < list.size()) {
                    model.detachProfileRef();
                    list.remove(i);
                    adapter.notifyItemRemoved(i);
                    break;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void addMessage(Chat chat) {
        list.add(chat);
        if (adapter != null) {
            adapter.notifyItemInserted(list.size() - 1);
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && !firebaseUser.getUid().equals(chat.getFrom())) {
            Log.i(TAG, "Read Message: " + chat.getBody() + " , from: " + chat.getFrom());
            chat.attachProfileListener(adapter, list.size() - 1);
        }
        int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        Log.i(TAG, "View position: " + viewPosition + " , count items: " + list.size());
        if (viewPosition >= list.size() - 3) {
            Log.i(TAG, "scroll to bottom");
            scrollToBottom();
        }
    }

    @Override
    public void scrollToBottom() {
        isLastItemHidden = false;
        if (adapter == null || recyclerView == null) {
            return;
        }
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
        }, 100);
    }

    @Override
    public void addChat(Chat chat) {
        Log.i(TAG,"Add Chat");
        mRunnable = new AddChatRunnable(chat);
        mHandler.postDelayed(mRunnable,700);
    }

    @Override
    public void updateChat(Chat chat) {
        Log.i(TAG,"Update Chat");
        for (int i = list.size() - 1; i >= 0; i--) {
            Chat model = list.get(i);
            if(model.getId().equals(chat.getId())){
                list.set(i, chat);
                Log.i(TAG,"Updated..!!");
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void removeChat(String idChat) {
        if(idChat == null){
            return;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            Chat model = list.get(i);
            if(model.getId().equals(idChat)){
                list.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            scrollY += dy;
            int viewPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            if (viewPosition < 0) {
                return;
            }
            isLastItemHidden = viewPosition < list.size() - 2;
//            Log.i(TAG, "On Scrolled -> is last scrolled hidden: " + isLastItemHidden +
//                    " , view position: " + viewPosition);
            if (!isLastItemHidden) {
                countBadge = 0;
                ((NearbyHomeActivity) getActivity()).showDownNotification(0); //hide double down icon
            }
        }
    };

    private void hideUnreadMessage() {
        Log.i(TAG, "Count items: " + list.size());
        for (int i = list.size() - 1; i >= 0; i--) {
            Chat model = list.get(i);
            if (model.getType() == Chat.TYPE_UNREAD_MESSAGE) {
                Log.i(TAG, "Hide unread position: " + i + " , text: " + model.getBody());
                list.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private final Handler mHandler = new Handler();
    private Runnable mRunnable;
    private class AddChatRunnable implements Runnable{
        private Chat chat;

        public AddChatRunnable(Chat chat){
            this.chat = chat;
        }

        @Override
        public void run() {
            if(chat == null){
                return;
            }
            list.add(chat);
            adapter.notifyItemChanged(list.size()-1);
            scrollToBottom();
        }
    }

    private Chat findChatById(String id) {
        if(id == null){
            return null;
        }
        for (Chat model : list) {
            if(model.getId() == null){
                continue;
            }
            if (model.getId().equals(id)) {
                Log.i(TAG,"Find chat id: "+model.getId()+" , body: "+model.getBody());
                return model;
            }
        }
        return null;
    }

//    private final Runnable showButtonDown = new Runnable() {
//        @Override
//        public void run() {
//            Chat lastModel = list.get(list.size()-1);
//            Log.i(TAG,"Last message: "+lastModel.getBody()+" , isShown: "+lastModel.isShown());
//        }
//    };
}
