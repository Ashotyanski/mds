package yandex.com.mds.hw.note_edit;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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

import static yandex.com.mds.hw.note_edit.NoteEditFragment.TRANSITION_NAME;

public class NotePagerFragment extends Fragment {
    public NotePagerFragment() {
    }

    public static NotePagerFragment newInstance(int notePosition, ArrayList<Note> notes, String transitionName) {
        Bundle args = new Bundle();
        args.putInt("NOTE_POS", notePosition);
        args.putParcelableArrayList("NOTES", notes);
        args.putString(TRANSITION_NAME, transitionName);
        NotePagerFragment fragment = new NotePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }
        setSharedElementReturnTransition(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_pager, container, false);
        Bundle args = getArguments();
        int currentPos = args.getInt("NOTE_POS");
        List<Note> notes = args.getParcelableArrayList("NOTES");
        final ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        String transitionName = args.getString(TRANSITION_NAME);
        final NotePagerAdapter adapter = new NotePagerAdapter(getChildFragmentManager(), notes, transitionName);
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
//        tabs.sc
//        tabs.setTabMode(TabLayout.MODE_FIXED);
        return view;
    }
}
