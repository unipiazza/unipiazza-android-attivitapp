package com.unipiazza.attivitapp.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.unipiazza.attivitapp.Prize;
import com.unipiazza.attivitapp.R;

public class GiftListAdapter extends BaseAdapter {

	private ArrayList<Prize> mProductList;
	private LayoutInflater inflater;
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "coins";
	private static final String TAG_ID_PRODUCT = "id";

	public GiftListAdapter(Context context, ArrayList<Prize> productList) {
		this.mProductList = productList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mProductList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mProductList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View v, ViewGroup parent) {
		ViewHolder holder;
		if (v == null) {
			v = inflater.inflate(R.layout.single_gift, parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) v.findViewById(R.id.name);
			holder.coins = (TextView) v
					.findViewById(R.id.coins);
			holder.icon = (ImageView) v
					.findViewById(R.id.icon);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}

		holder.icon.setImageResource(getImageByTitle(mProductList.get(arg0).getTitle()));
		holder.name.setText(mProductList.get(arg0).getName());
		holder.coins.setText(mProductList.get(arg0).getCoins() + "");
		return v;
	}

	private int getImageByTitle(String title) {
		if (title.equals("Panino") || title.equals("Hamburger"))
			return R.drawable.up_icon_hamburger;
		else if (title.equals("Spritz"))
			return R.drawable.up_icon_spritz;
		else if (title.equals("Caffè"))
			return R.drawable.up_icon_coffee;
		else if (title.equals("Cicchetto"))
			return R.drawable.up_icon_club_sandwich;
		else if (title.equals("Stickers"))
			return R.drawable.up_icon_sticker_small;
		else if (title.equals("Frappè") || title.equals("Spremuta"))
			return R.drawable.up_icon_juice;
		else if (title.equals("Birra"))
			return R.drawable.up_icon_beer;
		else if (title.equals("Prosecco"))
			return R.drawable.up_icon_wine;
		else if (title.equals("Cocktail"))
			return R.drawable.up_icon_cocktail;
		else if (title.equals("Toast"))
			return R.drawable.up_icon_toast;
		else if (title.equals("Limoncello"))
			return R.drawable.up_icon_limoncello;
		else if (title.equals("Shottino"))
			return R.drawable.up_icon_shots;
		else if (title.equals("Macedonia"))
			return R.drawable.up_icon_salad;
		else if (title.equals("Menù"))
			return R.drawable.icon_diamond;
		else if (title.equals("Bibita Analcolica"))
			return R.drawable.up_icon_coke;
		else if (title.equals("Trancio di Pizza"))
			return R.drawable.up_icon_pizza_slice;
		else if (title.equals("Tramezzino"))
			return R.drawable.up_icon_sandwich;
		else if (title.equals("Cartoleria") || title.equals("Stampa"))
			return R.drawable.up_icon_copying;
		else if (title.equals("Bott. Vino"))
			return R.drawable.up_icon_wine_bottle_glass;
		else if (title.equals("Caraffa Spritz"))
			return R.drawable.up_icon_beer_carafe;
		else if (title.equals("Gelato"))
			return R.drawable.up_icon_ice_cream;
		else if (title.equals("Abbigliamento"))
			return R.drawable.up_icon_clothing;
		else if (title.equals("Sconto"))
			return R.drawable.up_icon_discount;
		else if (title.equals("Maglietta Personalizzabile"))
			return R.drawable.up_icon_t_shirt;
		else if (title.equals("Penna"))
			return R.drawable.up_icon_pens;
		else if (title.equals("Kebab"))
			return R.drawable.up_icon_kebab;
		else if (title.equals("Cover Smartphone"))
			return R.drawable.up_icon_mobile;
		else if (title.equals("Libro"))
			return R.drawable.up_icon_book;
		else if (title.equals("Pizza"))
			return R.drawable.up_icon_pizza;
		return R.drawable.icon_diamond;
	}

	private static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView coins;
	}

}
