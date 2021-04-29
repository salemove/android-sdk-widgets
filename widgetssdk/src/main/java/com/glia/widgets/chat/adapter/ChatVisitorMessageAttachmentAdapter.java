package com.glia.widgets.chat.adapter;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.Glia;
import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.helper.Utils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

public class ChatVisitorMessageAttachmentAdapter extends ListAdapter<AttachmentFile, RecyclerView.ViewHolder> {
    private final static int IMAGE_VIEW_TYPE = 0;
    private final static int FILE_VIEW_TYPE = 1;

    public ChatVisitorMessageAttachmentAdapter() {
        super(new DiffUtil.ItemCallback<AttachmentFile>() {
            @Override
            public boolean areItemsTheSame(@NonNull AttachmentFile oldItem, @NonNull AttachmentFile newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AttachmentFile oldItem, @NonNull AttachmentFile newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                        oldItem.getContentType().equals(newItem.getContentType()) &&
                        oldItem.isDeleted() == newItem.isDeleted() &&
                        oldItem.getSize() == newItem.getSize();
            }
        });
    }

    private ItemCallback callback;

    public void setItemCallback(ItemCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        String mimeType = getItem(position).getContentType();
        if (mimeType.startsWith("image")) {
            return IMAGE_VIEW_TYPE;
        } else {
            return FILE_VIEW_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE_VIEW_TYPE)
            return new ImageViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_attachment_item_image, parent, false));
        else
            return new FileViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_attachment_item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ImageViewHolder holder1 = (ImageViewHolder) holder;
            holder1.onBind(getItem(position), callback);
        }
        if (holder instanceof FileViewHolder) {
            FileViewHolder holder1 = (FileViewHolder) holder;
            holder1.onBind(getItem(position), callback);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void onBind(AttachmentFile attachmentFile, ItemCallback callback) {
            ImageView imageView = itemView.findViewById(R.id.incoming_image_attachment);
            if (!attachmentFile.isDeleted()) {
                if (imageView.getTag() == null || !imageView.getTag().equals(attachmentFile.getName())) {

                    Glia.getCurrentEngagement().ifPresent(engagement -> engagement.fetchFile(attachmentFile.getId(),
                            (inputStream, e) -> {
                                Picasso picassoCustom
                                        = new Picasso.Builder(itemView.getContext()).addRequestHandler(new RequestHandler() {
                                    @Override
                                    public boolean canHandleRequest(Request data) {
                                        return true;
                                    }

                                    @Override
                                    public Result load(Request request, int networkPolicy) throws IOException {
                                        return new Result(inputStream, Picasso.LoadedFrom.NETWORK);
                                    }
                                }).build();
                                picassoCustom.load(attachmentFile.getName()).resize(0, 720).into(imageView);
                                imageView.setTag(attachmentFile.getName());
                            }
                    ));
                }
            }
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void onBind(AttachmentFile attachmentFile, ItemCallback callback) {
            CardView extensionContainerView = itemView.findViewById(R.id.type_indicator_view);
            TextView extensionTypeText = itemView.findViewById(R.id.type_indicator_text);

            TextView titleText = itemView.findViewById(R.id.item_title);
            TextView statusIndicator = itemView.findViewById(R.id.status_indicator);
            LinearProgressIndicator progressIndicator = itemView.findViewById(R.id.progress_indicator);

            itemView.setOnClickListener(
                    view -> {
                        itemView.setOnClickListener(null);
                        statusIndicator.setText("Downloading file...");
                        progressIndicator.setVisibility(View.VISIBLE);
                        // TODO itemcallback.onDownloadFile(id)
                    }
            );

            String name = attachmentFile.getName();
            long byteSize = attachmentFile.getSize();

            extensionContainerView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
            titleText.setText(String.format("%s • %s", name, Formatter.formatFileSize(itemView.getContext(), byteSize)));
            String extension = Utils.getExtensionByStringHandling(name).orElse("");
            extensionTypeText.setText(extension);
        }
    }

    private interface ItemCallback {

    }
}

