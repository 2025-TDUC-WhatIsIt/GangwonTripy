package com.whatisit.gangwontripy.ui.document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.whatisit.gangwontripy.R;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class DocumentFragment extends Fragment {
    private static final String ARG_DOCUMENT_TYPE = "document_type";

    // 어떤 종류의 문서를 보여줄지 결정하는 메소드
    public static DocumentFragment newInstance(String documentType) {
        DocumentFragment fragment = new DocumentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_TYPE, documentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_document, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleTextView = view.findViewById(R.id.tv_document_title);
        TextView contentTextView = view.findViewById(R.id.tv_document_content);

        if (getArguments() != null) {
            String documentType = getArguments().getString(ARG_DOCUMENT_TYPE);
            // titleTextView는 fragment_document.xml에 없으므로 삭제하거나 추가해야 합니다.

            if ("TERMS".equals(documentType)) {
                titleTextView.setText("약관 및 정책");
                // R.string.terms_content는 res/values/strings.xml에 저장된 긴 약관 내용
                contentTextView.setText(R.string.terms_content);
            } else if ("PRIVACY".equals(documentType)) {
                titleTextView.setText("개인정보 처리방침");
                contentTextView.setText(R.string.privacy_content);
            } else if ("LBS".equals(documentType)) {
                titleTextView.setText("위치기반 서비스 이용약관");
                contentTextView.setText(R.string.lbs_content);
            }
        }
    }

    // onResume에서 툴바 제목을 설정합니다.
    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null && getArguments() != null) {
                String documentType = getArguments().getString(ARG_DOCUMENT_TYPE);
                if ("TERMS".equals(documentType)) {
                    actionBar.setTitle("약관 및 정책");
                } else if ("PRIVACY".equals(documentType)) {
                    actionBar.setTitle("개인정보 처리방침");
                } else if ("LBS".equals(documentType)) {
                    actionBar.setTitle("위치기반 서비스 이용약관");
                }
            }
        }
    }
}
