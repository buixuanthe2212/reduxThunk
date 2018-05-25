package com.jinjerkeihi.scancard.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.LocaleSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jinjerkeihi.MainApplication;
import com.jinjerkeihi.R;
import com.jinjerkeihi.nfcfelica.transit.TransitData;
import com.jinjerkeihi.nfcfelica.transit.Trip;
import com.jinjerkeihi.nfcfelica.transit.orca.OrcaTrip;
import com.jinjerkeihi.nfcfelica.util.Utils;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XUAN_THE on 4/11/2018.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MyViewHolder> {

    private static final Pattern LINE_NUMBER = Pattern.compile("(#?\\d+)?(\\D.+)");
    private Trip[] moviesList;
    private Context mContext;
    private TransitData mTransitData;
    private IListener mIListener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, money, place, date, tvRouter;
        ImageView ivType, ivSelect;
        LinearLayout mLnCardInfo;
        View mView;

        MyViewHolder(View view) {
            super(view);
            money = view.findViewById(R.id.tvFare);
            place = view.findViewById(R.id.tvStation);
            date = view.findViewById(R.id.tvDate);
            tvRouter = view.findViewById(R.id.tvRouter);
            ivType = view.findViewById(R.id.iv_type_card);
            ivSelect = view.findViewById(R.id.iv_select);
            mLnCardInfo = view.findViewById(R.id.ln_card_info);
            mView = view.findViewById(R.id.view_line);
        }
    }

    public ResultAdapter(Context context, Trip[] moviesList, TransitData transitData, IListener iListener) {
        this.moviesList = moviesList;
        this.mContext = context;
        this.mTransitData = transitData;
        this.mIListener = iListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_info, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        boolean localisePlaces = MainApplication.localisePlaces();
        final Trip trip = moviesList[position];

        Log.e("startTimestamp", trip.getStartTimestamp().getTimeInMillis() + "");

//        holder.money.setText("¥" + trip.getFare().toString());
        holder.date.setText(Utils.isoDateFormat((GregorianCalendar) trip.getStartTimestamp()) + "(" + Utils.getDateOfWeek(trip.getStartTimestamp().getTime()).substring(0, 1) + ")");

        @DrawableRes int modeRes;
        @StringRes int modeContentDescriptionRes = 0;
        switch (trip.getMode()) {
            case BUS:
                modeRes = R.drawable.ic_bus;
                modeContentDescriptionRes = R.string.mode_bus;
                break;

            case TRAIN:
                modeRes = R.drawable.ic_train;
                modeContentDescriptionRes = R.string.mode_train;
                break;

            case TRAM:
                modeRes = R.drawable.tram;
                modeContentDescriptionRes = R.string.mode_tram;
                break;

            case METRO:
                modeRes = R.drawable.ic_train;
                modeContentDescriptionRes = R.string.mode_metro;
                break;

            case FERRY:
                modeRes = R.drawable.ferry;
                modeContentDescriptionRes = R.string.mode_ferry;
                break;

            case TICKET_MACHINE:
                modeRes = R.drawable.tvm;
                modeContentDescriptionRes = R.string.mode_ticket_machine;
                break;

            case VENDING_MACHINE:
                modeRes = R.drawable.ic_machine;
                modeContentDescriptionRes = R.string.mode_vending_machine;
                break;

            case POS:
                modeRes = R.drawable.cashier_yen;
                modeContentDescriptionRes = R.string.mode_pos;
                break;

            case BANNED:
                modeRes = R.drawable.banned;
                modeContentDescriptionRes = R.string.mode_banned;
                break;

            default:
                modeRes = R.drawable.unknown;
                modeContentDescriptionRes = R.string.mode_unknown;
                break;
        }
        holder.ivType.setImageResource(modeRes);
        String s = Utils.localizeString(modeContentDescriptionRes);
        if (localisePlaces && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SpannableString ss = new SpannableString(s);
            ss.setSpan(new LocaleSpan(Locale.getDefault()), 0, ss.length(), 0);
            holder.ivType.setContentDescription(ss);
        } else {
            holder.ivType.setContentDescription(s);
        }

        SpannableStringBuilder routeText = new SpannableStringBuilder();

        if (trip.getShortAgencyName() != null) {
            routeText.append(trip.getShortAgencyName())
                    .append(" ")
                    .setSpan(new StyleSpan(Typeface.NORMAL), 0, trip.getShortAgencyName().length(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                routeText.setSpan(new LocaleSpan(Locale.getDefault()), 0, routeText.length(), 0);
            }
        }

        if (trip.getRouteName() != null) {
            int oldLength = routeText.length();
            routeText.append(trip.getRouteName());
            if (localisePlaces && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (trip.getRouteLanguage() != null) {
                    // SUICA HACK:
                    // If there's something that looks like "#2" at the start, then mark
                    // that as the default language.
                    Matcher m = LINE_NUMBER.matcher(trip.getRouteName());
                    if (!m.find() || m.group(1) == null) {
                        // No line number
                        //Log.d(TAG, "no line number");
                        routeText.setSpan(new LocaleSpan(Locale.forLanguageTag(trip.getRouteLanguage())), oldLength, routeText.length(), 0);
                    } else {
                        // There is a line number
                        //Log.d(TAG, String.format("num = %s, line = %s", m.group(1), m.group(2)));
                        routeText.setSpan(new LocaleSpan(Locale.getDefault()), oldLength, oldLength + m.end(1), 0);
                        routeText.setSpan(new LocaleSpan(Locale.forLanguageTag(trip.getRouteLanguage())), oldLength + m.start(2), routeText.length(), 0);
                    }
                } else {
                    routeText.setSpan(new LocaleSpan(Locale.getDefault()), 0, routeText.length(), 0);
                }
            }
        }

        if (routeText.length() > 0) {
            holder.tvRouter.setText(routeText);
//            holder.tvRouter.setText(trip.getRouteName().toString());
            holder.tvRouter.setVisibility(View.VISIBLE);
        } else {
            holder.tvRouter.setVisibility(View.INVISIBLE);
        }

        Spannable stationText = Trip.formatStationNames(trip);
        if (stationText == null) {
            holder.place.setVisibility(View.INVISIBLE);
        } else {
            holder.place.setText(stationText);
            holder.place.setVisibility(View.VISIBLE);
        }

        holder.money.setVisibility(View.VISIBLE);
        if (trip.hasFare()) {
            holder.money.setText(mTransitData.formatCurrencyString(trip.getFare(), false));
        } else if (trip instanceof OrcaTrip) {
            holder.money.setText(R.string.pass_or_transfer);
        } else {
            // Hide the text "Fare" for hasFare == false
            holder.money.setVisibility(View.INVISIBLE);
        }

        if (trip.ismIsSelect()) {
            holder.ivSelect.setImageResource(R.drawable.ic_check);
        } else {
            holder.ivSelect.setImageResource(R.drawable.ic_uncheck);
        }

        holder.ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trip.setIsSelect(!trip.ismIsSelect());
                notifyDataSetChanged();
                if (mIListener != null) {
                    mIListener.onSelectItem(trip, trip.ismIsSelect());
                }
            }
        });

    }

    private void setVisibleItem(LinearLayout ln, View view, boolean isVisible) {
        if (isVisible) {
            ln.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        } else {
            ln.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return moviesList.length;
    }

    public interface IListener {
        void onSelectItem(Trip trip, boolean isSelect);
    }

}