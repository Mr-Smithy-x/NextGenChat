package co.yoprice.nextgenchat.ui.fragments;

import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import co.yoprice.nextgenchat.data.callbacks.FileCallback;
import co.yoprice.nextgenchat.data.imgurmodel.ImageResponse;
import co.yoprice.nextgenchat.data.models.PasteInfo;
import co.yoprice.nextgenchat.data.services.MediaService;
import co.yoprice.nextgenchat.helpers.IntentHelper;
import co.yoprice.nextgenchat.ui.custom.Snackbar;
import co.yoprice.nextgenchat.utils.MediaParser;
import co.yoprice.nextgenchat.data.models.StreamableVideo;
import co.yoprice.nextgenchat.ImageActivity;
import co.yoprice.nextgenchat.R;
import co.yoprice.nextgenchat.StreamableAcitivity;
import co.yoprice.nextgenchat.ui.adapters.MessageRecyclerAdapter;
import co.yoprice.nextgenchat.data.callbacks.NGUImgurCallback;
import co.yoprice.nextgenchat.data.callbacks.OnMessageListener;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.singleton.NotifSingleton;
import co.yoprice.nextgenchat.utils.NGUClient;
import co.yoprice.nextgenchat.PasteActivity;
import co.yoprice.nextgenchat.utils.SmartParse;
import co.yoprice.nextgenchat.utils.aLog;
import retrofit2.Response;

/**
 * Created by cj on 4/29/16.
 */
public class ChatFragment extends Fragment implements MediaParser.OnMediaCallBack {

    private static final int DELIM = 10;
    private NGUImgurCallback nguImgurCallback;
    private OnMessageListener messageListener;
    private RecyclerView recyclerView;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private Handler handler = new Handler();
    private AppCompatEditText mTextBox;
    private FloatingActionButton mSendBtn, mUpload;
    private android.support.design.widget.Snackbar snackbar;

    private FileCallback<ImageResponse> uploadBubble = new FileCallback<ImageResponse>() {
        @Override
        public void onResponse(retrofit2.Call<ImageResponse> call, Response<ImageResponse> response, File file) {
            final ImageResponse imageResponse = response.body();
            ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            if (imageResponse != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("ImageResponse", imageResponse.data.link));
                Snackbar.make(getView(), "Copied to clipboard!", Snackbar.LENGTH_LONG, ContextCompat.getColor(getContext(), R.color.md_green_700), ContextCompat.getColor(getContext(), R.color.md_white_1000))
                        .setAction("Share", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                IntentHelper.shareText(v.getContext(), imageResponse.data.link);
                            }
                        }).show();
            } else {
                onFailure(null, null, file);
            }
        }

        @Override
        public void onFailure(retrofit2.Call<ImageResponse> call, Throwable t, final File file) {
            co.yoprice.nextgenchat.ui.custom.Snackbar.make(getView(), "Could not upload Image", Snackbar.LENGTH_LONG, ContextCompat.getColor(getContext(), R.color.md_red_700), ContextCompat.getColor(getContext(), R.color.md_white_1000)).
                    setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MediaService.uploadImage(getContext(), file, uploadBubble);
                        }
                    }).show();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        mTextBox = (AppCompatEditText) view.findViewById(R.id.textBox);
        mSendBtn = (FloatingActionButton) view.findViewById(R.id.submitBtn);
        mUpload = (FloatingActionButton) view.findViewById(R.id.scrollBtn);
        recyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler);
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nguImgurCallback.OnSelectImageClicked();
            }
        });
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NGUClient.getInstance().shout(mTextBox.getText().toString());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageRecyclerAdapter = new MessageRecyclerAdapter() {
            @Override
            public void onMessageClicked(View view, int index) {
                final Message message = getMessageAtPosition(index);
                if (!message.isActionMessage()) {
                    //region NOT ACTION
                    if (message.getLink() != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (message.getLink().toLowerCase().endsWith(".png") || message.getLink().toLowerCase().endsWith(".jpeg") || message.getLink().toLowerCase().endsWith(".jpg") || message.getLink().toLowerCase().endsWith(".gif") || message.getLink().toLowerCase().endsWith(".bmp")) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            launchImageActivity(message.getLink());
                                        }
                                    });
                                } else {
                                    try {
                                        MediaParser.smartParse(message.getLink(), ChatFragment.this, message);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    } else {
                        aLog.i("MSG SIZE", String.valueOf(message.getMessage().length()));
                    }
                    //endregion
                } else {

                }
            }

            @Override
            public void onNotified(final int amount, final ArrayList<Message> messages) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (messageListener != null) {
                            SmartParse.parse(messages, ChatFragment.this.getContext(), messageListener);
                        }
                        scrollToLatestMessage(amount);
                    }
                });
            }

            @Override
            public void onNotified(int amount) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(getItemCount() - 1);
                    }
                });
            }

            @Override
            public boolean onPopupDialog(final PopupMenu popupMenu, final View view, int position) {
                final Message message = getMessageAtPosition(position);
                popupMenu.inflate(R.menu.popup_menu);
                final ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                if (message.getLink() == null) {
                    popupMenu.getMenu().removeItem(R.id.copy_link);
                    popupMenu.getMenu().removeGroup(R.id.extern_menu);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.open_extern:
                                IntentHelper.launchUrlIntent(message.getLink(), getActivity());
                                return true;
                            case R.id.copy_popup:
                                clipboardManager.setPrimaryClip(ClipData.newHtmlText("NGU", message.getUser() + " " + message.getTime() + " - " + message.getMessage(), message.getMessageElement()));
                                Snackbar.make(getView(),"Copied message to clipboard!",Snackbar.LENGTH_SHORT,ContextCompat.getColor(getContext(),R.color.md_green_700), ContextCompat.getColor(getContext(),R.color.md_green_700)).show();
                                return true;
                            case R.id.copy_link:
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("NGU", message.getLink()));
                                Snackbar.make(getView(),"Copied link to clipboard!",Snackbar.LENGTH_SHORT,ContextCompat.getColor(getContext(),R.color.md_green_700), ContextCompat.getColor(getContext(),R.color.md_green_700)).show();
                                return true;
                            case R.id.copy_share:
                                Bitmap bitmap = MediaService.getScreenShot(view);
                                IntentHelper.shareBitmap(getActivity(), bitmap, String.valueOf(System.currentTimeMillis()));
                                return true;
                            case R.id.copy_upload:
                                try {
                                    MediaService.uploadImage(view.getContext(), MediaService.getAnSaveScreenShot(view), uploadBubble);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
        return view;
    }

    public void clearTxtBox() {
        mTextBox.setText("");
    }

    private void scrollToLatestMessage(int amount) {
        Rect scrollBounds = new Rect();
        recyclerView.getHitRect(scrollBounds);
        View view = recyclerView.findChildViewUnder(scrollBounds.centerX(), scrollBounds.centerY());
        if (view != null) {
            int index = recyclerView.getChildAdapterPosition(view);
            if (index > -1) {
                if (messageRecyclerAdapter.getItemCount() > index && index > messageRecyclerAdapter.getItemCount() - DELIM) {
                    recyclerView.smoothScrollToPosition(messageRecyclerAdapter.getItemCount() - 1);
                } else {
                    if (snackbar == null)
                        snackbar = Snackbar.make(getView(), String.format("%s New message(s)", amount), Snackbar.LENGTH_SHORT, ContextCompat.getColor(getActivity(), R.color.colorPrimary), ContextCompat.getColor(getActivity(), R.color.md_white_1000));

                    if (!snackbar.isShown()) {
                        snackbar.setText(String.format("%s New message(s)", amount));
                        snackbar.setAction("Show", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                recyclerView.smoothScrollToPosition(messageRecyclerAdapter.getItemCount() - 1);
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        }
    }


    public void update(final ArrayList<Message> messages) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                messageRecyclerAdapter.addIfElegible(messages);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        messageListener = (OnMessageListener) NotifSingleton.getInstance();
        nguImgurCallback = (NGUImgurCallback) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        messageListener = null;
        nguImgurCallback = null;
        super.onDetach();
    }

    public boolean scrollToMessage(Message message) {
        int index = messageRecyclerAdapter.findMessagePosition(message);
        if (index != -1) {
            recyclerView.scrollToPosition(index);
        }
        return false;
    }

    public void setTextBox(String textBox) {
        String txt = mTextBox.getText().toString();
        mTextBox.setText(txt.length() > 0 ? txt + " " + textBox : textBox);
    }

    @Override
    public void onImageFound(final String url) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                launchImageActivity(url);
            }
        });
    }

    @Override
    public void onVideosFound(ArrayList<StreamableVideo> streamableVideos) {

    }

    @Override
    public void onVideoFound(final StreamableVideo streamableVideo) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                launchVideoActivity(streamableVideo);
            }
        });
    }

    private void launchImageActivity(String link) {
        Intent i = new Intent(getActivity(), ImageActivity.class);
        i.putExtra("url", link);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(i, ActivityOptions.makeBasic().toBundle());
        } else {
            startActivity(i);
        }
    }

    private void launchVideoActivity(StreamableVideo streamableVideo) {
        Intent i = new Intent(getActivity(), StreamableAcitivity.class);
        i.putExtra("video", streamableVideo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(i, ActivityOptions.makeBasic().toBundle());
        } else {
            startActivity(i);
        }
    }

    private void launchPasteActivity(PasteInfo pasteInfo, Message message){
        Intent intent = new Intent(getActivity(), PasteActivity.class);
        intent.putExtra(PasteActivity.PASTE_INFO, pasteInfo);
        intent.putExtra(PasteActivity.MESSAGE_INFO, message);
        startActivity(intent);
    }

    @Override
    public void onPasteInfoFound(final PasteInfo pasteInfo, final Message message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                launchPasteActivity(pasteInfo, message);
            }
        });
    }

    @Override
    public void onNothingFound(final String url) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                ab.setTitle("Confirmation");
                ab.setMessage(String.format("Do you want to open this link?\n%s", url));
                ab.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IntentHelper.launchUrlIntent(url, getActivity());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ab.show();
            }
        });
    }

    @Override
    public void onYoutubeVideoImageFound(String youtubeImage, final String url) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                NotifSingleton.getInstance().setOnMediaActivityOpened(true);
                IntentHelper.launchUrlIntent(url, getActivity());
            }
        });
    }

}
