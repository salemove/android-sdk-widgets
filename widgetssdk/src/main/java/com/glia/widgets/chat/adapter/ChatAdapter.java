package com.glia.widgets.chat.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.SingleChoiceCardView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import static com.glia.widgets.helper.PicassoUtils.loadImageFromDownloadsFolder;

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
                return ((OperatorStatusItem) oldItem).equals((OperatorStatusItem) newItem);
            } else if (oldItem instanceof VisitorMessageItem && newItem instanceof VisitorMessageItem) {
                return ((VisitorMessageItem) oldItem).equals((VisitorMessageItem) newItem);
            } else if (oldItem instanceof OperatorMessageItem && newItem instanceof OperatorMessageItem) {
                return ((OperatorMessageItem) oldItem).equals((OperatorMessageItem) newItem);
            } else if (oldItem instanceof MediaUpgradeStartedTimerItem && newItem instanceof MediaUpgradeStartedTimerItem) {
                return ((MediaUpgradeStartedTimerItem) oldItem).equals((MediaUpgradeStartedTimerItem) newItem);
            } else if (oldItem instanceof OperatorAttachmentItem && newItem instanceof OperatorAttachmentItem) {
                return ((OperatorAttachmentItem) oldItem).equals((OperatorAttachmentItem) newItem);
            } else if (oldItem instanceof VisitorAttachmentItem && newItem instanceof VisitorAttachmentItem) {
                return ((VisitorAttachmentItem) oldItem).equals((VisitorAttachmentItem) newItem);
            } else {
                return false;
            }
        }
    };

    private static final String TAG = ChatAdapter.class.getSimpleName();
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

    private static class OperatorStatusViewHolder extends RecyclerView.ViewHolder {
        private final OperatorStatusView statusPictureView;
        private final TextView chatStartingHeadingView;
        private final TextView chatStartingCaptionView;
        private final TextView chatStartedNameView;
        private final TextView chatStartedCaptionView;
        private final Context context;

        public OperatorStatusViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.statusPictureView = itemView.findViewById(R.id.status_picture_view);
            this.chatStartingHeadingView = itemView.findViewById(R.id.chat_starting_heading_view);
            this.chatStartingCaptionView = itemView.findViewById(R.id.chat_starting_caption_view);
            this.chatStartedNameView = itemView.findViewById(R.id.chat_started_name_view);
            this.chatStartedCaptionView = itemView.findViewById(R.id.chat_started_caption_view);

            context = itemView.getContext();

            statusPictureView.setTheme(uiTheme);
            chatStartingHeadingView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseDarkColor()));
            chatStartingCaptionView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
            chatStartedNameView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseDarkColor()));
            chatStartedCaptionView.setTextColor(ContextCompat.getColor(context, uiTheme.getBrandPrimaryColor()));

            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                chatStartingHeadingView.setTypeface(fontFamily, Typeface.BOLD);
                chatStartingCaptionView.setTypeface(fontFamily);
                chatStartedNameView.setTypeface(fontFamily, Typeface.BOLD);
                chatStartedCaptionView.setTypeface(fontFamily);
            }
        }

        public void bind(OperatorStatusItem item) {
            chatStartingHeadingView.setText(item.getCompanyName());
            if (item.getStatus() == OperatorStatusItem.Status.IN_QUEUE) {
                statusPictureView.showPlaceHolder();
                chatStartingHeadingView.setVisibility(View.VISIBLE);
                chatStartingCaptionView.setVisibility(View.VISIBLE);
                chatStartedNameView.setVisibility(View.GONE);
                chatStartedCaptionView.setVisibility(View.GONE);
            } else if (item.getStatus() == OperatorStatusItem.Status.OPERATOR_CONNECTED) {
                if (item.getProfileImgUrl() != null) {
                    statusPictureView.showProfileImage(item.getProfileImgUrl());
                } else {
                    statusPictureView.showPlaceHolder();
                }
                chatStartedNameView.setText(item.getOperatorName());
                chatStartedCaptionView.setText(context.getString(R.string.chat_operator_has_joined, item.getOperatorName()));

                chatStartingHeadingView.setVisibility(View.GONE);
                chatStartingCaptionView.setVisibility(View.GONE);
                chatStartedNameView.setVisibility(View.VISIBLE);
                chatStartedCaptionView.setVisibility(View.VISIBLE);
            }
            statusPictureView
                    .isRippleAnimationShowing(item.getStatus() == OperatorStatusItem.Status.IN_QUEUE);
        }
    }

    private static class VisitorMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;
        private final TextView deliveredView;
        //private final RecyclerView attachmentsView;

        public VisitorMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.content = itemView.findViewById(R.id.content);
            this.deliveredView = itemView.findViewById(R.id.delivered_view);
            //this.attachmentsView = itemView.findViewById(R.id.file_recycler_view);
            Context context = itemView.getContext();
            ColorStateList primaryBrandColor = ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
            content.setBackgroundTintList(primaryBrandColor);
            content.setTextColor(context.getColor(uiTheme.getBaseLightColor()));
            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                content.setTypeface(fontFamily);
                deliveredView.setTypeface(fontFamily);
            }
            deliveredView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
        }

        public void bind(VisitorMessageItem item) {
            content.setText(item.getMessage());
            deliveredView.setVisibility(item.isShowDelivered() ? View.VISIBLE : View.GONE);
        }
    }

    private static class OperatorMessageViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout contentLayout;
        private final UiTheme uiTheme;
        private final Context context;
        private final OperatorStatusView operatorStatusView;

        public OperatorMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.contentLayout = itemView.findViewById(R.id.content_layout);
            context = itemView.getContext();
            this.uiTheme = uiTheme;
            this.operatorStatusView = itemView.findViewById(R.id.chat_head_view);
            operatorStatusView.setTheme(uiTheme);
            operatorStatusView.isRippleAnimationShowing(false);
        }

        public void bind(
                OperatorMessageItem item,
                SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener,
                SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener
        ) {
            contentLayout.removeAllViews();
            if (item.singleChoiceOptions != null) {
                SingleChoiceCardView singleChoiceCardView = new SingleChoiceCardView(context);
                singleChoiceCardView.setOnOptionClickedListener(onOptionClickedListener);
                singleChoiceCardView.setData(
                        item.getId(),
                        item.choiceCardImageUrl,
                        item.content,
                        item.singleChoiceOptions,
                        item.selectedChoiceIndex,
                        uiTheme,
                        getAdapterPosition(),
                        item.selectedChoiceIndex == null ? onImageLoadedListener : null
                );
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(
                        0,
                        Float.valueOf(context.getResources().getDimension(R.dimen.medium))
                                .intValue(),
                        0,
                        0
                );
                contentLayout.addView(singleChoiceCardView, params);
            } else {
                TextView contentView = getMessageContentView();
                contentView.setText(item.content);
                contentLayout.addView(contentView);
            }
            operatorStatusView.setVisibility(item.showChatHead ? View.VISIBLE : View.GONE);
            if (item.operatorProfileImgUrl != null) {
                operatorStatusView.showProfileImage(item.operatorProfileImgUrl);
            } else {
                operatorStatusView.showPlaceHolder();
            }
        }

        private TextView getMessageContentView() {
            TextView contentView = (TextView) LayoutInflater.from(context)
                    .inflate(R.layout.chat_receive_message_content, contentLayout, false);
            ColorStateList operatorBgColor =
                    ContextCompat.getColorStateList(context, uiTheme.getSystemAgentBubbleColor());
            contentView.setBackgroundTintList(operatorBgColor);
            contentView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseDarkColor()));
            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                contentView.setTypeface(fontFamily);
            }
            return contentView;
        }
    }

    private static class MediaUpgradeStartedViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView timerView;
        private final @DrawableRes
        Integer upgradeAudioIcon;
        private final @DrawableRes
        Integer upgradeVideoIcon;

        public MediaUpgradeStartedViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            context = itemView.getContext();

            MaterialCardView layoutCardView = itemView.findViewById(R.id.card_view);
            iconView = itemView.findViewById(R.id.icon_view);
            titleView = itemView.findViewById(R.id.title_view);
            timerView = itemView.findViewById(R.id.timer_view);

            this.upgradeAudioIcon = uiTheme.getIconChatAudioUpgrade();
            this.upgradeVideoIcon = uiTheme.getIconChatVideoUpgrade();

            ColorStateList baseLightStateList = ContextCompat.getColorStateList(context, uiTheme.getBaseLightColor());
            int baseShadeColor = ContextCompat.getColor(context, uiTheme.getBaseShadeColor());
            int baseNormalColor = ContextCompat.getColor(context, uiTheme.getBaseNormalColor());
            ColorStateList brandPrimaryColorStateList =
                    ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
            int baseDarkColor = ContextCompat.getColor(context, uiTheme.getBaseDarkColor());

            layoutCardView.setBackgroundTintList(baseLightStateList);
            layoutCardView.setStrokeColor(baseShadeColor);
            iconView.setImageTintList(brandPrimaryColorStateList);
            titleView.setTextColor(baseDarkColor);
            timerView.setTextColor(baseNormalColor);

            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                titleView.setTypeface(fontFamily);
                timerView.setTypeface(fontFamily);
            }
        }

        public void bind(MediaUpgradeStartedTimerItem chatItem) {
            if (chatItem.type == MediaUpgradeStartedTimerItem.Type.AUDIO) {
                iconView.setImageResource(upgradeAudioIcon);
                titleView.setText(context.getString(R.string.chat_upgraded_to_audio_call));
            } else {
                iconView.setImageResource(upgradeVideoIcon);
                titleView.setText(context.getString(R.string.chat_upgraded_to_video_call));
            }
            timerView.setText(chatItem.time);
        }
    }

    static class FileAttachmentViewHolder extends RecyclerView.ViewHolder {

        OperatorStatusView operatorStatusView;

        public FileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);

            operatorStatusView = itemView.findViewById(R.id.chat_head_view);
            operatorStatusView.setTheme(uiTheme);
            operatorStatusView.isRippleAnimationShowing(false);
        }

        public void bind(OperatorAttachmentItem item) {
            setData(item.isFileExists, item.isDownloading, item.attachmentFile, item.showChatHead, item.operatorProfileImgUrl);
        }

        public void bind(VisitorAttachmentItem item) {
            setData(item.isFileExists, item.isDownloading, item.attachmentFile, false, null);
        }

        public void setData(boolean isFileExists, boolean isDownloading, AttachmentFile attachmentFile, boolean showChatHead, String operatorProfileImgUrl) {
            CardView extensionContainerView = itemView.findViewById(R.id.type_indicator_view);
            TextView extensionTypeText = itemView.findViewById(R.id.type_indicator_text);
            LinearProgressIndicator progressIndicator = itemView.findViewById(R.id.progress_indicator);
            TextView titleText = itemView.findViewById(R.id.item_title);
            TextView statusIndicator = itemView.findViewById(R.id.status_indicator);

            if (isFileExists) {
                statusIndicator.setText(R.string.chat_attachment_open_button_label);
            } else {
                statusIndicator.setText(R.string.chat_attachment_download_button_label);
            }

            if (isDownloading) {
                statusIndicator.setText(R.string.chat_attachment_downloading_label);
                progressIndicator.setVisibility(View.VISIBLE);
            } else {
                progressIndicator.setVisibility(View.GONE);
                if (isFileExists) {
                    statusIndicator.setText(R.string.chat_attachment_open_button_label);
                } else {
                    statusIndicator.setText(R.string.chat_attachment_download_button_label);
                }
            }

            String name = attachmentFile.getName();
            long byteSize = attachmentFile.getSize();

            extensionContainerView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
            titleText.setText(String.format("%s • %s", name, Formatter.formatFileSize(itemView.getContext(), byteSize)));
            String extension = Utils.getExtensionByStringHandling(name).orElse("");
            extensionTypeText.setText(extension);

            operatorStatusView.setVisibility(showChatHead ? View.VISIBLE : View.GONE);
            if (operatorProfileImgUrl != null) {
                operatorStatusView.showProfileImage(operatorProfileImgUrl);
            } else {
                operatorStatusView.showPlaceHolder();
            }
        }
    }

    static class ImageAttachmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageAttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.incoming_image_attachment);
        }

        public void bind(AttachmentFile attachmentFile) {
            Bitmap cachedBitmap = InAppFileCache.getInstance().getBitmapById(attachmentFile.getId() + "." + attachmentFile.getName());

            if (cachedBitmap != null) {
                imageView.setImageBitmap(cachedBitmap);
                Logger.d(TAG, "Image loaded from the in app file cache.");
            } else {
                Logger.d(TAG, "Image load from in app file cache failed, trying downloads folder.");
                loadImageFromDownloadsFolder(imageView.getContext(), attachmentFile, imageView);
            }
        }
    }

    public void submitList(List<ChatItem> items) {
        differ.submitList(items);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == OPERATOR_STATUS_VIEW_TYPE) {
            return new OperatorStatusViewHolder(
                    inflater.inflate(R.layout.chat_operator_status_layout, parent, false),
                    uiTheme);
        } else if (viewType == VISITOR_FILE_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.visitor_file_attachment_layout, parent, false);
            FileAttachmentViewHolder viewHolder = new FileAttachmentViewHolder(view, uiTheme);
            //addFileItemClickListener(viewHolder);

            return viewHolder;
        } else if (viewType == VISITOR_IMAGE_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.visitor_image_attachment_layout, parent, false);
            ImageAttachmentViewHolder viewHolder = new ImageAttachmentViewHolder(view);
            //addImageItemClickListener(viewHolder);

            return viewHolder;
        } else if (viewType == VISITOR_MESSAGE_TYPE) {
            return new VisitorMessageViewHolder(
                    inflater.inflate(R.layout.chat_visitor_message_layout, parent, false),
                    uiTheme);
        } else if (viewType == OPERATOR_IMAGE_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.chat_operator_image_attachment_layout, parent, false);
            ImageAttachmentViewHolder viewHolder = new ImageAttachmentViewHolder(view);
            addImageItemClickListener(viewHolder);

            return viewHolder;
        } else if (viewType == OPERATOR_FILE_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.chat_operator_file_attachment_layout, parent, false);
            FileAttachmentViewHolder viewHolder = new FileAttachmentViewHolder(view, uiTheme);
            addFileItemClickListener(viewHolder);

            return viewHolder;
        } else if (viewType == OPERATOR_MESSAGE_VIEW_TYPE) {
            return new OperatorMessageViewHolder(
                    inflater.inflate(R.layout.chat_operator_message_layout, parent, false),
                    uiTheme);
        } else if (viewType == MEDIA_UPGRADE_ITEM_TYPE) {
            return new MediaUpgradeStartedViewHolder(inflater.inflate(
                    R.layout.chat_media_upgrade_layout, parent, false),
                    uiTheme);
        } else {
            throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    private void addImageItemClickListener(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setOnClickListener(v -> {
            int pos = viewHolder.getAdapterPosition();
            ChatItem item = differ.getCurrentList().get(pos);

            if (!(item instanceof OperatorAttachmentItem) || pos == -1) {
                return;
            }

            onImageItemClickListener.onImageItemClick((OperatorAttachmentItem) item);
        });
    }

    private void addFileItemClickListener(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setOnClickListener(v -> {
            int pos = viewHolder.getAdapterPosition();
            ChatItem item = differ.getCurrentList().get(pos);

            if (!(item instanceof OperatorAttachmentItem) || pos == -1) {
                return;
            }

            if (((OperatorAttachmentItem) item).isFileExists) {
                onFileItemClickListener.onFileOpenClick((OperatorAttachmentItem) item);
            } else {
                onFileItemClickListener.onFileDownloadClick((OperatorAttachmentItem) item);
            }
        });
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
                ((FileAttachmentViewHolder) holder).bind((OperatorAttachmentItem) chatItem);
            } else {
                holder.setIsRecyclable(false);
                ((ImageAttachmentViewHolder) holder).bind(((OperatorAttachmentItem) chatItem).attachmentFile);
            }
        } else if (chatItem instanceof VisitorAttachmentItem) {
            if (chatItem.getViewType() == VISITOR_FILE_VIEW_TYPE) {
                ((FileAttachmentViewHolder) holder).bind((VisitorAttachmentItem) chatItem);
            } else {
                holder.setIsRecyclable(false);
                ((ImageAttachmentViewHolder) holder).bind(((VisitorAttachmentItem) chatItem).attachmentFile);
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

    public List<ChatItem> getCurrentList() {
        return differ.getCurrentList();
    }

    public interface OnFileItemClickListener {
        void onFileOpenClick(OperatorAttachmentItem item);

        void onFileDownloadClick(OperatorAttachmentItem item);
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(OperatorAttachmentItem item);
    }
}
