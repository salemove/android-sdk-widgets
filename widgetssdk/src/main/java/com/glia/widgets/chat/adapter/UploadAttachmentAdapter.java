package com.glia.widgets.chat.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.fileupload.model.FileAttachment;
import com.glia.widgets.helper.Utils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

public class UploadAttachmentAdapter extends ListAdapter<FileAttachment, UploadAttachmentAdapter.ViewHolder> {
    public UploadAttachmentAdapter() {
        super(new DiffUtil.ItemCallback<FileAttachment>() {
            @Override
            public boolean areItemsTheSame(@NonNull FileAttachment oldItem, @NonNull FileAttachment newItem) {
                return oldItem.getUri().equals(newItem.getUri());
            }

            @Override
            public boolean areContentsTheSame(@NonNull FileAttachment oldItem, @NonNull FileAttachment newItem) {
                return oldItem.isReadyToSend() == newItem.isReadyToSend() &&
                        oldItem.getAttachmentStatus().equals(newItem.getAttachmentStatus());
            }
        });
    }

    private ItemCallback callback;

    public void setItemCallback(ItemCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_attachment_uploaded_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(getItem(position), callback);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void onBind(FileAttachment attachment, ItemCallback callback) {
            CardView extensionContainerView = itemView.findViewById(R.id.type_indicator_view);
            TextView extensionTypeText = itemView.findViewById(R.id.type_indicator_text);
            ImageView extensionTypeImage = itemView.findViewById(R.id.type_indicator_image);

            TextView titleText = itemView.findViewById(R.id.item_title);
            TextView statusIndicator = itemView.findViewById(R.id.status_indicator);
            LinearProgressIndicator progressIndicator = itemView.findViewById(R.id.progress_indicator);

            ImageButton removeItemButton = itemView.findViewById(R.id.remove_item_button);
            removeItemButton.setOnClickListener(view -> {
                if (callback != null) callback.onRemoveItemClicked(attachment);
            });

            setProgressIndicatorState(progressIndicator, attachment.getAttachmentStatus());
            statusIndicator.setText(getStatusIndicatorText(itemView.getContext(), attachment.getAttachmentStatus()));

            ContentResolver contentResolver = itemView.getContext().getContentResolver();

            try (Cursor returnCursor = contentResolver.query(attachment.getUri(), null, null, null, null)) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();

                String name = returnCursor.getString(nameIndex);
                long byteSize = returnCursor.getLong(sizeIndex);

                setTitleText(titleText, name, byteSize, attachment.getAttachmentStatus());

                if (isError(attachment.getAttachmentStatus())) {
                    extensionContainerView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_system_agent_bubble_color));
                    extensionTypeImage.setVisibility(View.VISIBLE);
                    extensionTypeText.setVisibility(View.GONE);
                    extensionTypeImage.setImageResource(R.drawable.ic_info);
                    extensionTypeImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                } else {
                    extensionContainerView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
                    String mimeType = contentResolver.getType(attachment.getUri());
                    extensionTypeImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    if (mimeType.startsWith("image")) {
                        extensionTypeText.setVisibility(View.GONE);
                        extensionTypeImage.setVisibility(View.VISIBLE);
                        Picasso.with(itemView.getContext()).load(attachment.getUri()).into(extensionTypeImage);
                    } else {
                        extensionTypeImage.setVisibility(View.GONE);
                        extensionTypeText.setVisibility(View.VISIBLE);
                        String extension = Utils.getExtensionByStringHandling(name).orElse("");
                        extensionTypeText.setText(extension);
                    }
                }
            }
        }

        private boolean isError(FileAttachment.Status status) {
            switch (status) {
                case ERROR_NETWORK_TIMEOUT:
                case ERROR_INTERNAL:
                case ERROR_INVALID_INPUT:
                case ERROR_PERMISSIONS_DENIED:
                case ERROR_FORMAT_UNSUPPORTED:
                case ERROR_FILE_TOO_LARGE:
                case ERROR_ENGAGEMENT_MISSING:
                case ERROR_UNKNOWN:
                    return true;
                default:
                    return false;
            }
        }

        private void setTitleText(TextView titleText, String fileName, long byteSize, FileAttachment.Status status) {
            switch (status) {
                case UPLOADING:
                case SECURITY_SCAN:
                case READY_TO_SEND:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_base_normal_color));
                    titleText.setText(String.format("%s • %s", fileName, Formatter.formatFileSize(itemView.getContext(), byteSize)));
                    break;
                case ERROR_NETWORK_TIMEOUT:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_network_time_out);
                    break;
                case ERROR_INVALID_INPUT:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_invalid_input);
                    break;
                case ERROR_PERMISSIONS_DENIED:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_read_access_permissions_denied);
                    break;
                case ERROR_FORMAT_UNSUPPORTED:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_file_type_invalid);
                    break;
                case ERROR_FILE_TOO_LARGE:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_file_size_over_limit);
                    break;
                case ERROR_ENGAGEMENT_MISSING:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_engagement_missing);
                    break;
                case ERROR_UNKNOWN:
                case ERROR_INTERNAL:
                    titleText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    titleText.setText(R.string.chat_attachment_upload_error_internal_error);
                    break;
            }
        }

        private void setProgressIndicatorState(LinearProgressIndicator progressIndicator, FileAttachment.Status status) {
            switch (status) {
                case UPLOADING:
                    progressIndicator.setIndeterminate(true);
                    progressIndicator.setProgress(0);
                    progressIndicator.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
                    break;
                case SECURITY_SCAN:
                    progressIndicator.setIndeterminate(true);
                    progressIndicator.setProgress(50);
                    progressIndicator.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
                    break;
                case READY_TO_SEND:
                    progressIndicator.setIndeterminate(false);
                    progressIndicator.setProgress(100);
                    progressIndicator.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
                    break;
                case ERROR_NETWORK_TIMEOUT:
                case ERROR_INTERNAL:
                case ERROR_INVALID_INPUT:
                case ERROR_PERMISSIONS_DENIED:
                case ERROR_FORMAT_UNSUPPORTED:
                case ERROR_FILE_TOO_LARGE:
                case ERROR_ENGAGEMENT_MISSING:
                case ERROR_UNKNOWN:
                    progressIndicator.setIndeterminate(false);
                    progressIndicator.setProgress(100);
                    progressIndicator.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), R.color.design_default_color_error));
                    break;
            }
        }

        private String getStatusIndicatorText(Context context, FileAttachment.Status status) {
            switch (status) {
                case SECURITY_SCAN:
                    return context.getString(R.string.chat_attachment_upload_checking_file);
                case READY_TO_SEND:
                    return context.getString(R.string.chat_attachment_upload_ready_to_send);
                case ERROR_NETWORK_TIMEOUT:
                case ERROR_INTERNAL:
                case ERROR_INVALID_INPUT:
                case ERROR_PERMISSIONS_DENIED:
                case ERROR_FORMAT_UNSUPPORTED:
                case ERROR_FILE_TOO_LARGE:
                case ERROR_ENGAGEMENT_MISSING:
                case ERROR_UNKNOWN:
                    return context.getString(R.string.chat_attachment_upload_failed_upload);
                case UPLOADING:
                default:
                    return context.getString(R.string.chat_attachment_upload_uploading);
            }
        }
    }

    public interface ItemCallback {
        void onRemoveItemClicked(FileAttachment attachment);
    }
}
