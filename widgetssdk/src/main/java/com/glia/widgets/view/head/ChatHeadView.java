package com.glia.widgets.view.head;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.call.CallActivity;
import com.glia.widgets.chat.ChatActivity;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.view.configuration.ChatHeadConfiguration;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class ChatHeadView extends ConstraintLayout implements ChatHeadContract.View {
    private ShapeableImageView operatorImageView;
    private ShapeableImageView operatorPlaceholderImageView;
    private ShapeableImageView onHoldView;
    private TextView badgeView;
    private LottieAnimationView queueingAnimation;
    private GliaSdkConfiguration sdkConfiguration;
    private ChatHeadContract.Controller controller;
    private ChatHeadConfiguration configuration;

    public ChatHeadView(@NonNull Context context) {
        this(context, null);
    }

    public ChatHeadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public ChatHeadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public ChatHeadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(
                context,
                attrs,
                defStyleAttr,
                defStyleRes
        );
        init();
    }

    public static ChatHeadView getInstance(Context context) {
        return new ChatHeadView(context);
    }

    @Override
    public void setController(ChatHeadContract.Controller controller) {
        this.controller = controller;
    }

    @Override
    public void showUnreadMessageCount(int unreadMessageCount) {
        post(() -> {
            badgeView.setText(String.valueOf(unreadMessageCount));
            badgeView.setVisibility(isDisplayUnreadMessageBadge(unreadMessageCount));
        });
    }

    @Override
    public void showOperatorImage(String operatorProfileImgUrl) {
        post(() -> {
            queueingAnimation.setVisibility(GONE);
            operatorPlaceholderImageView.setVisibility(GONE);
            Picasso.get().load(operatorProfileImgUrl).into(operatorImageView);
        });
    }

    @Override
    public void showPlaceholder() {
        post(() -> {
            queueingAnimation.setVisibility(GONE);
            operatorImageView.setImageDrawable(null);
            operatorImageView.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), configuration.getBackgroundColorRes()));
            operatorPlaceholderImageView.setVisibility(VISIBLE);
        });
    }

    @Override
    public void showQueueing() {
        post(() -> {
            operatorPlaceholderImageView.setVisibility(GONE);
            operatorImageView.setImageDrawable(null);
            operatorImageView.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), configuration.getBadgeTextColor()));
            queueingAnimation.setVisibility(VISIBLE);
        });
    }

    @Override
    public void showOnHold() {
        post(() -> onHoldView.setVisibility(VISIBLE));
    }

    @Override
    public void hideOnHold() {
        post(() -> onHoldView.setVisibility(GONE));
    }

    @Override
    public void updateConfiguration(
            UiTheme buildTimeTheme,
            GliaSdkConfiguration sdkConfiguration
    ) {
        this.sdkConfiguration = sdkConfiguration;
        createHybridConfiguration(
                buildTimeTheme,
                sdkConfiguration
        );
        post(this::updateView);
    }

    @Override
    public void navigateToChat() {
        getContext().startActivity(
                getNavigationIntent(
                        getContext(),
                        ChatActivity.class,
                        sdkConfiguration
                )
        );
    }

    @Override
    public void navigateToCall() {
        getContext().startActivity(
                getNavigationIntent(
                        getContext(),
                        CallActivity.class,
                        sdkConfiguration
                )
        );
    }

    private ChatHeadConfiguration createBuildTimeConfiguration(UiTheme buildTimeTheme) {
        return ChatHeadConfiguration.builder()
                .operatorPlaceholderBackgroundColor(buildTimeTheme.getBrandPrimaryColor())
                .operatorPlaceholderIcon(buildTimeTheme.getIconPlaceholder())
                .operatorPlaceholderIconTintList(buildTimeTheme.getBaseLightColor())
                .badgeTextColor(buildTimeTheme.getBaseLightColor())
                .badgeBackgroundTintList(buildTimeTheme.getBrandPrimaryColor())
                .backgroundColorRes(buildTimeTheme.getBrandPrimaryColor())
                .iconOnHold(buildTimeTheme.getIconOnHold())
                .iconOnHoldTintList(buildTimeTheme.getBaseLightColor())
                .build();
    }

    private void createHybridConfiguration(
            UiTheme buildTimeTheme,
            GliaSdkConfiguration sdkConfiguration
    ) {
        configuration = createBuildTimeConfiguration(buildTimeTheme);
        if (sdkConfiguration == null) return;
        UiTheme runTimeTheme = sdkConfiguration.getRunTimeTheme();
        if (runTimeTheme == null) return;
        ChatHeadConfiguration runTimeConfiguration = runTimeTheme.getChatHeadConfiguration();
        ChatHeadConfiguration.Builder builder = ChatHeadConfiguration.builder(configuration);
        if (runTimeConfiguration != null) {
            if (runTimeConfiguration.getOperatorPlaceholderBackgroundColor() != null) {
                builder.operatorPlaceholderBackgroundColor(runTimeConfiguration.getOperatorPlaceholderBackgroundColor());
            }
            if (runTimeConfiguration.getOperatorPlaceholderIcon() != null) {
                builder.operatorPlaceholderIcon(runTimeConfiguration.getOperatorPlaceholderIcon());
            }
            if (runTimeConfiguration.getOperatorPlaceholderIconTintList() != null) {
                builder.operatorPlaceholderIconTintList(runTimeConfiguration.getOperatorPlaceholderIconTintList());
            }
            if (runTimeConfiguration.getBadgeBackgroundTintList() != null) {
                builder.badgeBackgroundTintList(runTimeConfiguration.getBadgeBackgroundTintList());
            }
            if (runTimeConfiguration.getBadgeTextColor() != null) {
                builder.badgeTextColor(runTimeConfiguration.getBadgeTextColor());
            }
            if (runTimeConfiguration.getBackgroundColorRes() != null) {
                builder.backgroundColorRes(runTimeConfiguration.getBackgroundColorRes());
            }
            if (runTimeConfiguration.getIconOnHold() != null) {
                builder.iconOnHold(runTimeConfiguration.getIconOnHold());
            }
            if (runTimeConfiguration.getIconOnHoldTintList() != null) {
                builder.iconOnHoldTintList(runTimeConfiguration.getIconOnHoldTintList());
            }
        }
        configuration = builder.build();
    }

    private void init() {
        View view = View.inflate(this.getContext(), R.layout.chat_head_view, this);
        operatorImageView = view.findViewById(R.id.profile_picture_view);
        operatorPlaceholderImageView = view.findViewById(R.id.placeholder_view);
        badgeView = view.findViewById(R.id.chat_bubble_badge);
        queueingAnimation = view.findViewById(R.id.queueing_lottie_animation);
        onHoldView = view.findViewById(R.id.on_hold_icon);
    }

    private void updateOperatorPlaceholderImageView() {
        operatorPlaceholderImageView
                .setImageResource(
                        configuration.getOperatorPlaceholderIcon()
                );

        operatorPlaceholderImageView
                .setBackgroundColor(
                        ContextCompat
                                .getColor(
                                        this.getContext(),
                                        configuration
                                                .getOperatorPlaceholderBackgroundColor()
                                )
                );

        operatorPlaceholderImageView
                .setImageTintList(
                        ContextCompat.getColorStateList(
                                this.getContext(),
                                configuration
                                        .getOperatorPlaceholderIconTintList()
                        )
                );
    }

    private void updateOnHoldImageView() {
        onHoldView.setImageResource(
                configuration.getIconOnHold()
        );

        onHoldView.setImageTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        configuration.getIconOnHoldTintList()
                )
        );
    }

    private void updateBadgeView() {
        badgeView.setBackgroundTintList(
                ContextCompat.getColorStateList(
                        this.getContext(),
                        configuration
                                .getBadgeBackgroundTintList()
                )
        );
        badgeView.setTextColor(
                ContextCompat.getColor(
                        this.getContext(),
                        configuration
                                .getBadgeTextColor()
                )
        );
    }

    private void updateOperatorImageView() {
        operatorImageView.setBackgroundColor(
                ContextCompat.getColor(
                        this.getContext(),
                        configuration.getBackgroundColorRes()
                )
        );
    }

    private void updateQueueingAnimationView() {
        queueingAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), this.configuration.getBackgroundColorRes()), PorterDuff.Mode.SRC_OVER)
        );
    }

    private void updateView() {
        updateOperatorPlaceholderImageView();
        updateOnHoldImageView();
        updateBadgeView();
        updateOperatorImageView();
        updateQueueingAnimationView();
    }

    private static Intent getNavigationIntent(Context context, Class<?> cls, GliaSdkConfiguration sdkConfiguration) {
        Intent newIntent = new Intent(context, cls);
        newIntent.putExtra(GliaWidgets.COMPANY_NAME, sdkConfiguration.getCompanyName());
        newIntent.putExtra(GliaWidgets.QUEUE_ID, sdkConfiguration.getQueueId());
        newIntent.putExtra(GliaWidgets.CONTEXT_URL, sdkConfiguration.getContextUrl());
        newIntent.putExtra(GliaWidgets.UI_THEME, sdkConfiguration.getRunTimeTheme());
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }

    private int isDisplayUnreadMessageBadge(int unreadMessageCount) {
        return unreadMessageCount > 0 ? VISIBLE : GONE;
    }
}
