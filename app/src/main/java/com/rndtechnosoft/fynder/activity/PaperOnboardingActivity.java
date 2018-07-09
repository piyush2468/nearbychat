package com.rndtechnosoft.fynder.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.rndtechnosoft.fynder.R;
import com.rndtechnosoft.fynder.onboarding.PaperOnboardingEngine;
import com.rndtechnosoft.fynder.onboarding.PaperOnboardingPage;
import com.rndtechnosoft.fynder.onboarding.listeners.PaperOnboardingOnChangeListener;
import com.rndtechnosoft.fynder.onboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

public class PaperOnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_main_layout);

        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener(new PaperOnboardingOnChangeListener() {
            @Override
            public void onPageChanged(int oldElementIndex, int newElementIndex) {
//                Toast.makeText(getApplicationContext(), "Swiped from " + oldElementIndex + " to " + newElementIndex, Toast.LENGTH_SHORT).show();
            }
        });

        engine.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
            @Override
            public void onRightOut() {
                // Probably here will be your exit action

//                Toast.makeText(getApplicationContext(), "Swiped out right", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Just example data for Onboarding
    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage("Discover", "Discover singles in your area now.",
                Color.parseColor("#678FB4"), R.drawable.ic_ob_discover, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("Chat", "Setup real dates & Talk to singles around the world.",
                Color.parseColor("#65B0B4"), R.drawable.ic_ob_chat, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("Meet", "Meet people looking for somthing real.",
                Color.parseColor("#9B90BC"), R.drawable.ic_ob_meet, R.drawable.onboarding_pager_circle_icon);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }
}
