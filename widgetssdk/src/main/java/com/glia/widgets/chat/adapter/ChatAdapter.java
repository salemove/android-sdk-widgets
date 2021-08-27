package com.glia.widgets.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.model.history.ChatItem;
import com.glia.widgets.chat.model.history.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.chat.model.history.OperatorMessageItem;
import com.glia.widgets.chat.model.history.OperatorStatusItem;
import com.glia.widgets.chat.model.history.VisitorAttachmentItem;
import com.glia.widgets.chat.model.history.VisitorMessageItem;
import com.glia.widgets.chat.adapter.holder.FileAttachmentViewHolder;
import com.glia.widgets.chat.adapter.holder.ImageAttachmentViewHolder;
import com.glia.widgets.chat.adapter.holder.MediaUpgradeStartedViewHolder;
import com.glia.widgets.chat.adapter.holder.OperatorMessageViewHolder;
import com.glia.widgets.chat.adapter.holder.OperatorStatusViewHolder;
import com.glia.widgets.chat.adapter.holder.VisitorMessageViewHolder;
import com.glia.widgets.view.SingleChoiceCardView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final AsyncListDiffer<ChatItem> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);

    public static final DiffUtil.ItemCallback<ChatItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            if (oldItem instanceof OperatorStatusItem && newItem instanceof OperatorStatusItem) {
                return oldItem.equals(newItem);
            } else if (oldItem instanceof VisitorMessageItem && newItem instanceof VisitorMessageItem) {
                return oldItem.equals(newItem);
            } else if (oldItem instanceof OperatorMessageItem && newItem instanceof OperatorMessageItem) {
                return oldItem.equals(newItem);
            } else if (oldItem instanceof MediaUpgradeStartedTimerItem && newItem instanceof MediaUpgradeStartedTimerItem) {
                return oldItem.equals(newItem);
            } else if (oldItem instanceof OperatorAttachmentItem && newItem instanceof OperatorAttachmentItem) {
                return oldItem.equals(newItem);
            } else if (oldItem instanceof VisitorAttachmentItem && newItem instanceof VisitorAttachmentItem) {
                return oldItem.equals(newItem);
            } else {
                return false;
            }
        }
    };

    public static final int OPERATOR_STATUS_VIEW_TYPE = 0;
    public static final int VISITOR_MESSAGE_TYPE = 1;
    public static final int OPERATOR_MESSAGE_VIEW_TYPE = 2;
    public static final int MEDIA_UPGRADE_ITEM_TYPE = 3;
    public static final int OPERATOR_FILE_VIEW_TYPE = 4;
    public static final int OPERATOR_IMAGE_VIEW_TYPE = 5;
    public static final int VISITOR_FILE_VIEW_TYPE = 6;
    public static final int VISITOR_IMAGE_VIEW_TYPE = 7;

    private final UiTheme uiTheme;
    private final SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener;
    private final SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener;
    private final OnFileItemClickListener onFileItemClickListener;
    private final OnImageItemClickListener onImageItemClickListener;

    public ChatAdapter(
            UiTheme uiTheme,
            SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener,
            SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener,
            OnFileItemClickListener onFileItemClickListener, OnImageItemClickListener onImageItemClickListener) {
        this.uiTheme = uiTheme;
        this.onOptionClickedListener = onOptionClickedListener;
        this.onImageLoadedListener = onImageLoadedListener;
        this.onFileItemClickListener = onFileItemClickListener;
        this.onImageItemClickListener = onImageItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == OPERATOR_STATUS_VIEW_TYPE) {
            return new OperatorStatusViewHolder(inflater.inflate(R.layout.chat_operator_status_layout, parent, false), uiTheme);
        } else if (viewType == VISITOR_FILE_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.chat_attachment_visitor_file_layout, parent, false);
            return new FileAttachmentViewHolder(view, uiTheme);
        } else if (viewType == VISITOR_IMAGE_VIEW_TYPE) {
            return new ImageAttachmentViewHolder(inflater.inflate(R.layout.chat_attachment_visitor_image_layout, parent, false));
        } else if (viewType == VISITOR_MESSAGE_TYPE) {
            return new VisitorMessageViewHolder(inflater.inflate(R.layout.chat_visitor_message_layout, parent, false), uiTheme);
        } else if (viewType == OPERATOR_IMAGE_VIEW_TYPE) {
            return new ImageAttachmentViewHolder(inflater.inflate(R.layout.chat_attachment_operator_image_layout, parent, false));
        } else if (viewType == OPERATOR_FILE_VIEW_TYPE) {
            return new FileAttachmentViewHolder(inflater.inflate(R.layout.chat_attachment_operator_file_layout, parent, false), uiTheme);
        } else if (viewType == OPERATOR_MESSAGE_VIEW_TYPE) {
            return new OperatorMessageViewHolder(inflater.inflate(R.layout.chat_operator_message_layout, parent, false), uiTheme);
        } else if (viewType == MEDIA_UPGRADE_ITEM_TYPE) {
            return new MediaUpgradeStartedViewHolder(inflater.inflate(R.layout.chat_media_upgrade_layout, parent, false), uiTheme);
        } else {
            throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem chatItem = differ.getCurrentList().get(position);
        if (chatItem instanceof OperatorStatusItem) {
            ((OperatorStatusViewHolder) holder).bind((OperatorStatusItem) chatItem);
        } else if (chatItem instanceof VisitorMessageItem) {
            ((VisitorMessageViewHolder) holder).bind((VisitorMessageItem) chatItem);
        } else if (chatItem instanceof OperatorMessageItem) {
            ((OperatorMessageViewHolder) holder).bind((OperatorMessageItem) chatItem, onOptionClickedListener, onImageLoadedListener);
        } else if (chatItem instanceof MediaUpgradeStartedTimerItem) {
            ((MediaUpgradeStartedViewHolder) holder).bind((MediaUpgradeStartedTimerItem) chatItem);
        } else if (chatItem instanceof OperatorAttachmentItem) {
            if (chatItem.getViewType() == OPERATOR_FILE_VIEW_TYPE) {
                FileAttachmentViewHolder viewHolder = (FileAttachmentViewHolder) holder;
                OperatorAttachmentItem item = (OperatorAttachmentItem) chatItem;
                viewHolder.bind(item);
                viewHolder.itemView.setOnClickListener(v -> {
                    if (item.isFileExists) {
                        onFileItemClickListener.onFileOpenClick(item.attachmentFile);
                    } else {
                        onFileItemClickListener.onFileDownloadClick(item.attachmentFile);
                    }
                });
            } else {
                //holder.setIsRecyclable(false);
                ImageAttachmentViewHolder viewHolder = (ImageAttachmentViewHolder) holder;
                AttachmentFile file = ((OperatorAttachmentItem) chatItem).attachmentFile;
                viewHolder.bind(file);
                viewHolder.itemView.setOnClickListener(v -> onImageItemClickListener.onImageItemClick(file));
            }
        } else if (chatItem instanceof VisitorAttachmentItem) {
            if (chatItem.getViewType() == VISITOR_FILE_VIEW_TYPE) {
                FileAttachmentViewHolder viewHolder = (FileAttachmentViewHolder) holder;
                VisitorAttachmentItem item = (VisitorAttachmentItem) chatItem;
                viewHolder.bind(item);
                viewHolder.itemView.setOnClickListener(v -> {
                    if (item.isFileExists) {
                        onFileItemClickListener.onFileOpenClick(item.attachmentFile);
                    } else {
                        onFileItemClickListener.onFileDownloadClick(item.attachmentFile);
                    }
                });
            } else {
                //holder.setIsRecyclable(false);
                ImageAttachmentViewHolder viewHolder = (ImageAttachmentViewHolder) holder;
                AttachmentFile file = ((VisitorAttachmentItem) chatItem).attachmentFile;
                viewHolder.bind(file);
                viewHolder.itemView.setOnClickListener(v -> onImageItemClickListener.onImageItemClick(file));
            }
        }
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public int getItemViewType(int position) {
        return differ.getCurrentList().get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);

        if (holder instanceof ImageAttachmentViewHolder) {
            ((ImageAttachmentViewHolder) holder).onStopView();
        }
    }

    public void submitList(List<ChatItem> items) {
        differ.submitList(items);
    }

    public List<ChatItem> getCurrentList() {
        return differ.getCurrentList();
    }

    public interface OnFileItemClickListener {
        void onFileOpenClick(AttachmentFile file);

        void onFileDownloadClick(AttachmentFile file);
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(AttachmentFile item);
    }
}
