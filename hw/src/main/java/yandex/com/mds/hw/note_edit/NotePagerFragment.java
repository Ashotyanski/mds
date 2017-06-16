package yandex.com.mds.hw.note_edit;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.transition.Fade;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import yandex.com.mds.hw.R;
import yandex.com.mds.hw.models.Note;

public class NotePagerFragment extends Fragment {
    private static final String TAG = NotePagerFragment.class.getName();
    private ViewPager pager;
    private NotePagerAdapter adapter;

    public NotePagerFragment() {
    }

    public static NotePagerFragment newInstance(int notePosition, ArrayList<Note> notes) {
        Bundle args = new Bundle();
        args.putInt("NOTE_POS", notePosition);
        args.putParcelableArrayList("NOTES", notes);
        NotePagerFragment fragment = new NotePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setEnterSharedElementCallback(new SharedElementCallback() {
//                @Override
//                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                    super.onMapSharedElements(names, sharedElements);
//                    Log.d(TAG, "onMapSharedElements: " + names);
//                    View view = (pager.getChildAt(pager.getCurrentItem())).findViewById(R.id.color);
//                    if (view != null) {
//                        sharedElements.remove(names.get(0));
//                        names.set(0, ViewCompat.getTransitionName(view));
//                        sharedElements.put(names.get(0), view);
//                    }
//                }
//            });
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
            setSharedElementReturnTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
            setExitTransition(new Fade(Fade.IN));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_pager, container, false);
        Bundle args = getArguments();
        int currentPos = args.getInt("NOTE_POS");
        List<Note> notes = args.getParcelableArrayList("NOTES");
        pager = (ViewPager) view.findViewById(R.id.pager);
        adapter = new NotePagerAdapter(getChildFragmentManager(), notes);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < adapter.getCount(); i++) {
                    adapter.getItem(i).setHasOptionsMenu(i == position);
                }
            }
        });
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentPos);

        TabLayout tabs = (TabLayout) view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager, true);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        return view;
    }
}
