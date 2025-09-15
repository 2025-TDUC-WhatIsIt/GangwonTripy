package com.whatisit.gangwontripy.ui.mypage.badge;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.whatisit.gangwontripy.data.model.BadgeItem;
import com.whatisit.gangwontripy.R;

public class BadgeDetailDialogFragment extends DialogFragment {

    private static final String ARG_BADGE_NAME = "badge_name";
    private static final String ARG_BADGE_DESC = "badge_desc";
    private static final String ARG_BADGE_IMAGE = "badge_image";

    public static BadgeDetailDialogFragment newInstance(BadgeItem badge) {
        BadgeDetailDialogFragment fragment = new BadgeDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BADGE_NAME, badge.getName());
        args.putString(ARG_BADGE_DESC, badge.getDescription());
        args.putInt(ARG_BADGE_IMAGE, badge.getImageResourceId());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_badge_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 찾기
        ImageView badgeImage = view.findViewById(R.id.iv_dialog_badge_image);
        TextView badgeName = view.findViewById(R.id.tv_dialog_badge_name);
        TextView badgeDesc = view.findViewById(R.id.tv_dialog_badge_desc);
        Button applyButton = view.findViewById(R.id.btn_apply_badge);
        ImageButton closeButton = view.findViewById(R.id.btn_close);

        // 데이터 채우기
        if (getArguments() != null) {
            badgeName.setText(getArguments().getString(ARG_BADGE_NAME));
            badgeDesc.setText(getArguments().getString(ARG_BADGE_DESC));
            badgeImage.setImageResource(getArguments().getInt(ARG_BADGE_IMAGE));
        }

        // 클릭 리스너
        closeButton.setOnClickListener(v -> dismiss()); // dismiss()로 팝업 닫기
        applyButton.setOnClickListener(v -> {
            // TODO: '적용하기' 로직 구현
            Toast.makeText(getContext(), badgeName.getText() + " 배지를 적용했습니다.", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // 팝업 창 크기 및 스타일 조절
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}