package co.yoprice.nextgenchat.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import co.yoprice.nextgenchat.R;
import co.yoprice.nextgenchat.data.models.Color;
import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.data.models.NGUSmilies;
import co.yoprice.nextgenchat.data.models.PasteInfo;
import co.yoprice.nextgenchat.utils.MediaParser;
import co.yoprice.nextgenchat.utils.NGUClient;
import co.yoprice.nextgenchat.data.models.StreamableVideo;
import co.yoprice.nextgenchat.utils.aLog;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cj on 4/24/16.
 */
public abstract class MessageRecyclerAdapter extends RecyclerView.Adapter {

    private static final int ACTION = 5;
    private final int LEFT = 0, RIGHT = 1, IMAGE = 2, RIGHT_IMAGE = 3, VIDEO = 4;
    private Handler handler = new Handler();
    private ArrayList<Message> messageList = new ArrayList<>();
    private boolean firstime = true;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LEFT) {
            return new MessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message, parent, false));
        } else if (viewType == RIGHT) {
            return new MessageRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_end, parent, false));
        } else if (viewType == IMAGE) {
            return new MessageImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_image, parent, false));
        } else if (viewType == RIGHT_IMAGE) {
            return new MessageRightImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_image_end, parent, false));
        } else if(viewType == ACTION){
            return new ActionMessageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_action, parent, false));
        }else return null;
    }

    public abstract void onMessageClicked(View view, int index);
    public abstract void onNotified(int amount, ArrayList<Message> messages);
    public abstract void onNotified(int amount);
    public abstract boolean onPopupDialog(PopupMenu popupMenu, View view, int position);

    @Override
    public int getItemViewType(int position) {
        if(!getMessageAtPosition(position).isActionMessage()) {
            if (getMessageAtPosition(position).getLink() == null) {
                try {
                    if (NGUClient.getInstance().getUserId().equals(getMessageAtPosition(position).getUserId()))
                        return RIGHT;
                    else return LEFT;
                } catch (Exception ex) {
                    return LEFT;
                }
            } else {
                try {
                    if (checkIfValidExtension(getMessageAtPosition(position))) {
                        if (NGUClient.getInstance().getUserId().equals(getMessageAtPosition(position).getUserId()))
                            return RIGHT_IMAGE;
                        else return IMAGE;
                    } else if (NGUClient.getInstance().getUserId().equals(getMessageAtPosition(position).getUserId()))
                        return RIGHT;
                    else return LEFT;
                } catch (Exception ex) {
                    return LEFT;
                }
            }
        }else{
            return ACTION;
        }
    }

    public String cleanseUrl(String url) {
        int index = url.indexOf("?");
        if (index > -1) {
            return url.substring(0, index);
        } else return url;
    }

    private boolean checkIfValidExtension(Message messageAtPosition) {
        String link = messageAtPosition.getLink();
        return cleanseUrl(link).toLowerCase().contains("screencloud.net/v/") ||
                cleanseUrl(link).toLowerCase().contains("streamable.com") ||
                cleanseUrl(link).toLowerCase().contains("gyazo.com") ||
                cleanseUrl(link).toLowerCase().contains("imgur.com") && !cleanseUrl(link).toLowerCase().endsWith(".gifv") ||
                cleanseUrl(link).toLowerCase().contains("prntscr.com") ||
                cleanseUrl(link).toLowerCase().contains("prnt.sc") ||
                cleanseUrl(link).toLowerCase().endsWith(".png") ||
                cleanseUrl(link).toLowerCase().endsWith(".jpg") ||
                cleanseUrl(link).toLowerCase().endsWith(".jpeg") ||
                cleanseUrl(link).toLowerCase().endsWith(".gif") ||
                cleanseUrl(link).toLowerCase().endsWith(".bmp") ||
                cleanseUrl(link).toLowerCase().contains("youtube.com") ||
                cleanseUrl(link).toLowerCase().contains("youtu.be");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //region LEFT
        if (getItemViewType(position) == LEFT) {
            Color user = getMessageAtPosition(position).getUser_color(), message = getMessageAtPosition(position).getMessageColor();
            ((MessageHolder) holder).user.setText(getMessageAtPosition(position).getUser());
            ((MessageHolder) holder).user.setTextColor(android.graphics.Color.rgb(user.getR(), user.getG(), user.getB()));
            if (getMessageAtPosition(position).getMessageElement() == null) {
                ((MessageHolder) holder).message.setText(getMessageAtPosition(position).getMessage());
            } else {
                Spanned htmlSpan = Html.fromHtml(getMessageAtPosition(position).getMessageElement(), new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return getDraw(((MessageHolder) holder).getItemView().getContext(), source);
                    }
                }, null);
                ((MessageHolder) holder).message.setText(htmlSpan);
            }
            ((MessageHolder) holder).message.setTextColor(android.graphics.Color.rgb(message.getR(), message.getG(), message.getB()));
            ((MessageHolder) holder).date.setText(getMessageAtPosition(position).getTime());
            Picasso.with(((MessageHolder) holder).circleImageView.getContext()).load(NGUClient.getInstance().generateUserIcon(getMessageAtPosition(position).getUserId())).error(R.mipmap.ngu).into(((MessageHolder) holder).circleImageView);
        }//endregion
        //region RIGHT
        else if (getItemViewType(position) == RIGHT) {
            Color user = getMessageAtPosition(position).getUser_color(), message = getMessageAtPosition(position).getMessageColor();
            ((MessageRightHolder) holder).user.setText(getMessageAtPosition(position).getUser());
            ((MessageRightHolder) holder).user.setTextColor(android.graphics.Color.rgb(user.getR(), user.getG(), user.getB()));
            if (getMessageAtPosition(position).getMessageElement() == null) {
                ((MessageRightHolder) holder).message.setText(getMessageAtPosition(position).getMessage());
            } else {

                Spanned htmlSpan = Html.fromHtml(getMessageAtPosition(position).getMessageElement(), new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return getDraw(((MessageRightHolder) holder).itemView.getContext(), source);
                    }
                }, null);
                ((MessageRightHolder) holder).message.setText(htmlSpan);
            }
            ((MessageRightHolder) holder).message.setTextColor(android.graphics.Color.rgb(message.getR(), message.getG(), message.getB()));
            ((MessageRightHolder) holder).date.setText(getMessageAtPosition(position).getTime());
            Picasso.with(((MessageRightHolder) holder).circleImageView.getContext()).load(NGUClient.getInstance().generateUserIcon(getMessageAtPosition(position).getUserId())).error(R.mipmap.ngu).into(((MessageRightHolder) holder).circleImageView);
        }//endregion
        //region IMAGE
        else if (getItemViewType(position) == IMAGE) {
            final Color user = getMessageAtPosition(position).getUser_color(), message = getMessageAtPosition(position).getMessageColor();
            ((MessageImageHolder) holder).user.setText(getMessageAtPosition(position).getUser());
            ((MessageImageHolder) holder).user.setTextColor(android.graphics.Color.rgb(user.getR(), user.getG(), user.getB()));
            if (getMessageAtPosition(position).getMessageElement() == null) {
                ((MessageImageHolder) holder).message.setText(getMessageAtPosition(position).getMessage());
            } else {
                Spanned htmlSpan = Html.fromHtml(getMessageAtPosition(position).getMessageElement(), new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return getDraw(((MessageImageHolder) holder).itemView.getContext(), source);
                    }
                }, null);
                ((MessageImageHolder) holder).message.setText(htmlSpan);
            }
            ((MessageImageHolder) holder).message.setTextColor(android.graphics.Color.rgb(message.getR(), message.getG(), message.getB()));
            ((MessageImageHolder) holder).date.setText(getMessageAtPosition(position).getTime());
            Picasso.with(((MessageImageHolder) holder).circleImageView.getContext()).load(NGUClient.getInstance().generateUserIcon(getMessageAtPosition(position).getUserId())).error(R.mipmap.ngu).into(((MessageImageHolder) holder).circleImageView);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".png") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".jpeg") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".jpg") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".gif") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".bmp")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Picasso.with(((MessageImageHolder) holder).appCompatImageView.getContext()).load(getMessageAtPosition(position).getLink()).error(R.mipmap.ngu).into(((MessageImageHolder) holder).appCompatImageView);
                            }
                        });
                    } else {
                        try {
                            MediaParser.smartParse(getMessageAtPosition(position).getLink(), new MediaParser.OnMediaCallBack() {
                                @Override
                                public void onImageFound(final String url) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Picasso.with(((MessageImageHolder) holder).appCompatImageView.getContext()).load(url).error(R.mipmap.ngu).into(((MessageImageHolder) holder).appCompatImageView);
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
                                            Picasso.with(((MessageImageHolder) holder).appCompatImageView.getContext()).load(streamableVideo.getJpg()).error(R.mipmap.ngu).into(((MessageImageHolder) holder).appCompatImageView);
                                        }
                                    });

                                }

                                @Override
                                public void onPasteInfoFound(PasteInfo pasteInfo, Message message) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void onNothingFound(String url) {

                                }

                                @Override
                                public void onYoutubeVideoImageFound(final String youtubeImage, String url) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Picasso.with(((MessageImageHolder) holder).appCompatImageView.getContext()).load(youtubeImage).error(R.mipmap.ngu).into(((MessageImageHolder) holder).appCompatImageView);
                                        }
                                    });
                                }
                            }, getMessageAtPosition(position));
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        //endregion
        //region RIGHT IMAGE
        else if (getItemViewType(position) == RIGHT_IMAGE) {
            final Color user = getMessageAtPosition(position).getUser_color(), message = getMessageAtPosition(position).getMessageColor();
            ((MessageRightImageHolder) holder).user.setText(getMessageAtPosition(position).getUser());
            ((MessageRightImageHolder) holder).user.setTextColor(android.graphics.Color.rgb(user.getR(), user.getG(), user.getB()));
            if (getMessageAtPosition(position).getMessageElement() == null) {
                ((MessageRightImageHolder) holder).message.setText(getMessageAtPosition(position).getMessage());
            } else {
                Spanned htmlSpan = Html.fromHtml(getMessageAtPosition(position).getMessageElement(), new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return getDraw(((MessageRightImageHolder) holder).itemView.getContext(), source);
                    }
                }, null);
                ((MessageRightImageHolder) holder).message.setText(htmlSpan);
            }
            ((MessageRightImageHolder) holder).message.setTextColor(android.graphics.Color.rgb(message.getR(), message.getG(), message.getB()));
            ((MessageRightImageHolder) holder).date.setText(getMessageAtPosition(position).getTime());
            Picasso.with(((MessageRightImageHolder) holder).circleImageView.getContext()).load(NGUClient.getInstance().generateUserIcon(getMessageAtPosition(position).getUserId())).error(R.mipmap.ngu).into(((MessageRightImageHolder) holder).circleImageView);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".png") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".jpeg") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".jpg") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".gif") || cleanseUrl(getMessageAtPosition(position).getLink()).endsWith(".bmp")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Picasso.with(((MessageRightImageHolder) holder).appCompatImageView.getContext()).load(getMessageAtPosition(position).getLink()).error(R.mipmap.ngu).into(((MessageRightImageHolder) holder).appCompatImageView);
                            }
                        });
                    } else {
                        try {
                            MediaParser.smartParse(getMessageAtPosition(position).getLink(), new MediaParser.OnMediaCallBack() {
                                @Override
                                public void onImageFound(final String url) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Picasso.with(((MessageRightImageHolder) holder).appCompatImageView.getContext()).load(url).error(R.mipmap.ngu).into(((MessageRightImageHolder) holder).appCompatImageView);
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
                                            Picasso.with(((MessageRightImageHolder) holder).appCompatImageView.getContext()).load(streamableVideo.getJpg()).error(R.mipmap.ngu).into(((MessageRightImageHolder) holder).appCompatImageView);
                                        }
                                    });

                                }

                                @Override
                                public void onPasteInfoFound(PasteInfo pasteInfo, Message message) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void onNothingFound(String url) {

                                }

                                @Override
                                public void onYoutubeVideoImageFound(final String youtubeImage, String url) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Picasso.with(((MessageRightImageHolder) holder).appCompatImageView.getContext()).load(youtubeImage).error(R.mipmap.ngu).into(((MessageRightImageHolder) holder).appCompatImageView);
                                        }
                                    });
                                }
                            }, getMessageAtPosition(position));
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        //endregion
        //region ACTION
        else if(getItemViewType(position) == ACTION){
            ((ActionMessageHolder)holder).message.setText(Html.fromHtml(getMessageAtPosition(position).getMessageElement(), new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    return getDraw(holder.itemView.getContext(), source);
                }
            },null));
        }
        //endregion
    }

    private Drawable getDraw(Context context, String source) {
        Drawable bmp = getResource(context, source);
        bmp.setBounds(0, 0, bmp.getIntrinsicWidth(), bmp.getIntrinsicHeight());
        return bmp;
    }

    private Drawable getResource(Context context, String source) {
        NGUSmilies nguSmilies = MediaParser.getSmilies(source);
        if (nguSmilies != null) {
            aLog.e("Smilies", nguSmilies.getFile_name());
            try {
                int drawable = MediaParser.getSmliesId(nguSmilies);
                if (drawable != -1) {
                    aLog.e("Smilie ID", String.valueOf(drawable));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        return context.getDrawable(drawable);
                    } else {
                        return context.getResources().getDrawable(drawable);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(R.drawable.smile);
        } else return context.getResources().getDrawable(R.drawable.smile);
    }

    public void addMessage(Message message) {
        messageList.add(0, message);
        notifyItemInserted(0);
    }

    private void addReverseMessage(Message m) {
        messageList.add(m);
        notifyItemInserted(getItemCount());
    }

    public void clearAll() {
        int size = getItemCount();
        messageList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public Message getMessageAtPosition(int index) {
        return messageList.get(index);
    }

    public ArrayList<Message> getMessageList() {
        return messageList;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public boolean addIfElegible(ArrayList<Message> mess) {
        try {
            if (firstime) {
                int added = 0;
                for (Message m : mess) {
                    if (!contains(m)) {
                        addMessage(m);
                        added++;
                    }
                }
                firstime = false;
                if (added > 0) {
                    aLog.i("Added", "Added Messages: " + String.valueOf(added));
                    onNotified(added);
                    return true;
                } else return false;
            } else {
                int added = 0;
                ArrayList<Message> messages = new ArrayList<>();
                for (Message m : mess) {
                    if (!contains(m)) {
                        messages.add(m);
                        added++;
                    }
                }
                Collections.reverse(messages);
                for (Message m : messages) {
                    addReverseMessage(m);
                }
                if (added > 0) {
                    aLog.i("Added", "Added Messages: " + String.valueOf(added));
                    onNotified(added, messages);
                    return true;
                } else return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean contains(Message mess) {
        if(mess.isActionMessage()){
            for (Message m : getMessageList()) {
                if (m.getMessage().equalsIgnoreCase(mess.getMessage())) {
                    return true;
                }
            }
        }else {
            for (Message m : getMessageList()) {
                if (m.getMessage().equalsIgnoreCase(mess.getMessage()) && m.getTime().equalsIgnoreCase(mess.getTime()) && m.getUser().equalsIgnoreCase(mess.getUser())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int findMessagePosition(Message message) {
        if (messageList.contains(message)) {
            return messageList.indexOf(message);
        } else return -1;
    }

    public class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView user, message, date;
        CircleImageView circleImageView;
        CardView cardView;

        public MessageHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_layout);
            user = (AppCompatTextView) itemView.findViewById(R.id.message_user);
            message = (AppCompatTextView) itemView.findViewById(R.id.message_text);
            date = (AppCompatTextView) itemView.findViewById(R.id.message_date);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.circle_image_icon);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        public View getItemView() {
            return itemView;
        }

        @Override
        public void onClick(View v) {
            onMessageClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return onPopupDialog(new PopupMenu(v.getContext(), v, GravityCompat.END),v, getAdapterPosition());
        }
    }

    public class MessageRightHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView user, message, date;
        CircleImageView circleImageView;
        CardView cardView;

        public MessageRightHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_layout);
            user = (AppCompatTextView) itemView.findViewById(R.id.message_user);
            message = (AppCompatTextView) itemView.findViewById(R.id.message_text);
            date = (AppCompatTextView) itemView.findViewById(R.id.message_date);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.circle_image_icon);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMessageClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return onPopupDialog(new PopupMenu(v.getContext(), v, GravityCompat.END),v, getAdapterPosition());
        }
    }

    public class MessageImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView user, message, date;
        CircleImageView circleImageView;
        CardView cardView;
        AppCompatImageView appCompatImageView;

        public MessageImageHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_layout);
            user = (AppCompatTextView) itemView.findViewById(R.id.message_user);
            message = (AppCompatTextView) itemView.findViewById(R.id.message_text);
            date = (AppCompatTextView) itemView.findViewById(R.id.message_date);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.circle_image_icon);
            appCompatImageView = (AppCompatImageView) itemView.findViewById(R.id.card_image_display);
            cardView.setOnClickListener(this);
            appCompatImageView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            appCompatImageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMessageClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return onPopupDialog(new PopupMenu(v.getContext(), v, GravityCompat.END),v, getAdapterPosition());
        }
    }

    private class MessageRightImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatTextView user, message, date;
        CircleImageView circleImageView;
        CardView cardView;
        AppCompatImageView appCompatImageView;

        public MessageRightImageHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_layout);
            user = (AppCompatTextView) itemView.findViewById(R.id.message_user);
            message = (AppCompatTextView) itemView.findViewById(R.id.message_text);
            date = (AppCompatTextView) itemView.findViewById(R.id.message_date);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.circle_image_icon);
            appCompatImageView = (AppCompatImageView) itemView.findViewById(R.id.card_image_display);
            cardView.setOnClickListener(this);
            appCompatImageView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
            appCompatImageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMessageClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return onPopupDialog(new PopupMenu(v.getContext(), v, GravityCompat.END),v, getAdapterPosition());
        }
    }

    private class ActionMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AppCompatTextView message;
        public ActionMessageHolder(View view) {
            super(view);
            message = (AppCompatTextView) view.findViewById(R.id.message_user);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMessageClicked(v,getAdapterPosition());
        }
    }
}
