package com.whatisit.gangwontripy.ui.main.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.MagazineRes;

import android.widget.ImageView;
import android.widget.TextView;

public class MagazineDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_MAG = "arg_mag";

    public static MagazineDetailBottomSheet newInstance(MagazineRes m) {
        Bundle b = new Bundle();
        b.putSerializable(ARG_MAG, m);
        MagazineDetailBottomSheet f = new MagazineDetailBottomSheet();
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_magazine_detail, container, false);
    }

    @Override public void onStart() {
        super.onStart();
        // 풀 확장
        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(parent);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            behavior.setFitToContents(true);
        }
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        MagazineRes m = (MagazineRes) (getArguments() != null ? getArguments().getSerializable(ARG_MAG) : null);
        if (m == null) { dismissAllowingStateLoss(); return; }

        ImageView img = v.findViewById(R.id.img_cover);
        TextView badge = v.findViewById(R.id.tv_badge);
        TextView head  = v.findViewById(R.id.tv_headline);
        TextView place = v.findViewById(R.id.tv_place);
        TextView body  = v.findViewById(R.id.tv_body);
        TextView credit= v.findViewById(R.id.tv_credit);
        View btnLink   = v.findViewById(R.id.btn_open_link);
        View btnClose  = v.findViewById(R.id.btn_close);

        Glide.with(this).load(m.imageUrl).into(img);
        badge.setText((m.season != null ? m.season : "") + (m.topic != null ? " · " + m.topic : ""));
        head.setText(m.headline);
        place.setText(m.placeName);

        // 본문에 HTML 태그가 올 수도 있어 처리
        if (!TextUtils.isEmpty(m.body) && (m.body.contains("<") && m.body.contains(">"))) {
            body.setText(Html.fromHtml(m.body, Html.FROM_HTML_MODE_LEGACY));
            body.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            body.setText(m.body);
        }

        credit.setText(!TextUtils.isEmpty(m.credit) ? m.credit : "");

        if (TextUtils.isEmpty(m.placeLink)) {
            btnLink.setVisibility(View.GONE);
        } else {
            btnLink.setOnClickListener(v1 -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(m.placeLink)));
            });
        }

        btnClose.setOnClickListener(v12 -> dismiss());
    }
}
