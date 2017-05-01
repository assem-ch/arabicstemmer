public class StringBuffer {
	private String b = "";

	@Override
	public int length() {
		return b.length();
	}

	void replace(int start, int end, String str) {
		if (start == 0 && end == b.length()) {
			b = str;
		} else {
			String left = b.substring(0, start), right = b.substring(end);
			b = left + str + right;
		}
	}

	String substring(int start, int end) {
		return b.substring(start, end);
	}

	@Override
	public char charAt(int index) {
		return b.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		throw new Error("NotImpl: CharSequence::subSequence");
	}

	@Override
	public String toString() {
		return b;
	}

}

public class StringBuilder extends StringBuffer {

}


public class Among {

	private static char[] toCharArray(String s) {
		int sLength = s.length();
		char[] charArr = new char[sLength];
		for (int i = 0; i < sLength; i++)
			charArr[i] = s.charAt(i);
		return charArr;
	}

	public Among(String s, int substring_i, int result, String methodname, Class obj) {
		this.s = toCharArray(s);
		this.substring_i = substring_i;
		this.result = result;
		method = methodname ? obj[methodname] : null;
		methodobject = obj;
	}

	public final char[] s; /* search string */
	public final int substring_i; /* index to longest matching substring */
	public final int result; /* result of the lookup */
	public final Method method; /* method to use if substring matches */
	public Object methodobject; /* object to invoke method on */
};



public class SnowballProgram {
	protected SnowballProgram() {
		current = new StringBuffer();
		setCurrent("");
	}

	/**
	 * Set the current string.
	 */
	public void setCurrent(String value) {
		current.replace(0, current.length(), value);
		cursor = 0;
		limit = current.length();
		limit_backward = 0;
		bra = cursor;
		ket = limit;
	}

	/**
	 * Get the current string.
	 */
	public String getCurrent() {
		String result = current.toString();
		// Make a new StringBuffer. If we reuse the old one, and a user of
		// the library keeps a reference to the buffer returned (for example,
		// by converting it to a String in a way which doesn't force a copy),
		// the buffer size will not decrease, and we will risk wasting a large
		// amount of memory.
		// Thanks to Wolfram Esser for spotting this problem.
		current = new StringBuffer();
		return result;
	}

	// current string
	protected StringBuffer current;

	protected int cursor;
	protected int limit;
	protected int limit_backward;
	protected int bra;
	protected int ket;

	protected boolean in_grouping(char[] s, int min, int max) {
		if (cursor >= limit)
			return false;
		char ch = current.charAt(cursor);
		if (ch > max || ch < min)
			return false;
		ch -= min;
		if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0)
			return false;
		cursor++;
		return true;
	}

	protected boolean in_grouping_b(char[] s, int min, int max) {
		if (cursor <= limit_backward)
			return false;
		char ch = current.charAt(cursor - 1);
		if (ch > max || ch < min)
			return false;
		ch -= min;
		if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0)
			return false;
		cursor--;
		return true;
	}

	protected boolean out_grouping(char[] s, int min, int max) {
		if (cursor >= limit)
			return false;
		char ch = current.charAt(cursor);
		if (ch > max || ch < min) {
			cursor++;
			return true;
		}
		ch -= min;
		if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
			cursor++;
			return true;
		}
		return false;
	}

	protected boolean out_grouping_b(char[] s, int min, int max) {
		if (cursor <= limit_backward)
			return false;
		char ch = current.charAt(cursor - 1);
		if (ch > max || ch < min) {
			cursor--;
			return true;
		}
		ch -= min;
		if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
			cursor--;
			return true;
		}
		return false;
	}

	protected boolean eq_s(String s) {
		if (limit - cursor < s.length())
			return false;
		int i;
		for (i = 0; i != s.length(); i++) {
			if (current.charAt(cursor + i) != s.charAt(i))
				return false;
		}
		cursor += s.length();
		return true;
	}

	protected boolean eq_s_b(String s) {
		if (cursor - limit_backward < s.length())
			return false;
		int i;
		for (i = 0; i != s.length(); i++) {
			if (current.charAt(cursor - s.length() + i) != s.charAt(i))
				return false;
		}
		cursor -= s.length();
		return true;
	}

	protected int find_among(Among v[]) {
		int i = 0;
		int j = v.length;

		int c = cursor;
		int l = limit;

		int common_i = 0;
		int common_j = 0;

		boolean first_key_inspected = false;

		while (true) {
			int k = i + ((j - i) >> 1);
			int diff = 0;
			int common = common_i < common_j ? common_i : common_j; // smaller
			Among w = v[k];
			int i2;
			for (i2 = common; i2 < w.s.length; i2++) {
				if (c + common == l) {
					diff = -1;
					break;
				}
				diff = current.charAt(c + common) - w.s[i2];
				if (diff != 0)
					break;
				common++;
			}
			if (diff < 0) {
				j = k;
				common_j = common;
			} else {
				i = k;
				common_i = common;
			}
			if (j - i <= 1) {
				if (i > 0)
					break; // v->s has been inspected
				if (j == i)
					break; // only one item in v

				// - but now we need to go round once more to get
				// v->s inspected. This looks messy, but is actually
				// the optimal approach.

				if (first_key_inspected)
					break;
				first_key_inspected = true;
			}
		}
		while (true) {
			Among w = v[i];
			if (common_i >= w.s.length) {
				cursor = c + w.s.length;
				if (w.method == null)
					return w.result;

				boolean res;
				res = w.method.call(w.methodobject);
				cursor = c + w.s.length;
				if (res)
					return w.result;
			}
			i = w.substring_i;
			if (i < 0)
				return 0;
		}
	}

	// find_among_b is for backwards processing. Same comments apply
	protected int find_among_b(Among v[]) {
		int i = 0;
		int j = v.length;

		int c = cursor;
		int lb = limit_backward;

		int common_i = 0;
		int common_j = 0;

		boolean first_key_inspected = false;

		while (true) {
			int k = i + ((j - i) >> 1);
			int diff = 0;
			int common = common_i < common_j ? common_i : common_j;
			Among w = v[k];
			int i2;
			for (i2 = w.s.length - 1 - common; i2 >= 0; i2--) {
				if (c - common == lb) {
					diff = -1;
					break;
				}
				diff = current.charAt(c - 1 - common) - w.s[i2];
				if (diff != 0)
					break;
				common++;
			}
			if (diff < 0) {
				j = k;
				common_j = common;
			} else {
				i = k;
				common_i = common;
			}
			if (j - i <= 1) {
				if (i > 0)
					break;
				if (j == i)
					break;
				if (first_key_inspected)
					break;
				first_key_inspected = true;
			}
		}
		while (true) {
			Among w = v[i];
			if (common_i >= w.s.length) {
				cursor = c - w.s.length;
				if (w.method == null)
					return w.result;

				boolean res;
				res = w.method.call(w.methodobject);
				cursor = c - w.s.length;
				if (res)
					return w.result;
			}
			i = w.substring_i;
			if (i < 0)
				return 0;
		}
	}

	/*
	 * to replace chars between c_bra and c_ket in current by the chars in s.
	 */
	protected int replace_s(int c_bra, int c_ket, String s) {
		int adjustment = s.length() - (c_ket - c_bra);
		current.replace(c_bra, c_ket, s);
		limit += adjustment;
		if (cursor >= c_ket)
			cursor += adjustment;
		else if (cursor > c_bra)
			cursor = c_bra;
		return adjustment;
	}

	protected void slice_check() {
		if (bra < 0 || bra > ket || ket > limit || limit > current.length()) // this
																				// line
																				// could
																				// be
																				// removed
		{
			throw new Error("Snowball: faulty slice operation");
		}
	}

	protected void slice_from(String s) {
		slice_check();
		replace_s(bra, ket, s);
	}

	protected void slice_del() {
		slice_from("");
	}

	protected void insert(int c_bra, int c_ket, String s) {
		int adjustment = replace_s(c_bra, c_ket, s);
		if (c_bra <= bra)
			bra += adjustment;
		if (c_bra <= ket)
			ket += adjustment;
	}

	/* Copy the slice into the supplied StringBuilder */
	protected StringBuilder slice_to(StringBuilder s) {
		slice_check();
		s.replace(0, s.length(), current.substring(bra, ket));
		return s;
	}
};


public abstract class SnowballStemmer extends SnowballProgram {
	public abstract boolean stem();
};
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class arabicStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("!", -1, 3), new Among("\"", -1, 3), new Among("%", -1, 3),
			new Among("*", -1, 3), new Among(",", -1, 3), new Among(".", -1, 3), new Among("/", -1, 3),
			new Among(":", -1, 3), new Among(";", -1, 3), new Among("?", -1, 3), new Among("\\", -1, 3),
			new Among("\u060C", -1, 4), new Among("\u061B", -1, 4), new Among("\u061F", -1, 4),
			new Among("\u0640", -1, 2), new Among("\u064B", -1, 1), new Among("\u064C", -1, 1),
			new Among("\u064D", -1, 1), new Among("\u064E", -1, 1), new Among("\u064F", -1, 1),
			new Among("\u0650", -1, 1), new Among("\u0651", -1, 1), new Among("\u0652", -1, 1),
			new Among("\u0660", -1, 5), new Among("\u0661", -1, 6), new Among("\u0662", -1, 7),
			new Among("\u0663", -1, 8), new Among("\u0664", -1, 9), new Among("\u0665", -1, 10),
			new Among("\u0666", -1, 11), new Among("\u0667", -1, 12), new Among("\u0668", -1, 13),
			new Among("\u0669", -1, 14), new Among("\u066A", -1, 15), new Among("\u066B", -1, 15),
			new Among("\u066C", -1, 15), new Among("\uFE80", -1, 16), new Among("\uFE81", -1, 20),
			new Among("\uFE82", -1, 20), new Among("\uFE83", -1, 17), new Among("\uFE84", -1, 17),
			new Among("\uFE85", -1, 21), new Among("\uFE86", -1, 21), new Among("\uFE87", -1, 18),
			new Among("\uFE88", -1, 18), new Among("\uFE89", -1, 19), new Among("\uFE8A", -1, 19),
			new Among("\uFE8B", -1, 19), new Among("\uFE8C", -1, 19), new Among("\uFE8D", -1, 22),
			new Among("\uFE8E", -1, 22), new Among("\uFE8F", -1, 23), new Among("\uFE90", -1, 23),
			new Among("\uFE91", -1, 23), new Among("\uFE92", -1, 23), new Among("\uFE93", -1, 24),
			new Among("\uFE94", -1, 24), new Among("\uFE95", -1, 25), new Among("\uFE96", -1, 25),
			new Among("\uFE97", -1, 25), new Among("\uFE98", -1, 25), new Among("\uFE99", -1, 26),
			new Among("\uFE9A", -1, 26), new Among("\uFE9B", -1, 26), new Among("\uFE9C", -1, 26),
			new Among("\uFE9D", -1, 27), new Among("\uFE9E", -1, 27), new Among("\uFE9F", -1, 27),
			new Among("\uFEA0", -1, 27), new Among("\uFEA1", -1, 28), new Among("\uFEA2", -1, 28),
			new Among("\uFEA3", -1, 28), new Among("\uFEA4", -1, 28), new Among("\uFEA5", -1, 29),
			new Among("\uFEA6", -1, 29), new Among("\uFEA7", -1, 29), new Among("\uFEA8", -1, 29),
			new Among("\uFEA9", -1, 30), new Among("\uFEAA", -1, 30), new Among("\uFEAB", -1, 31),
			new Among("\uFEAC", -1, 31), new Among("\uFEAD", -1, 32), new Among("\uFEAE", -1, 32),
			new Among("\uFEAF", -1, 33), new Among("\uFEB0", -1, 33), new Among("\uFEB1", -1, 34),
			new Among("\uFEB2", -1, 34), new Among("\uFEB3", -1, 34), new Among("\uFEB4", -1, 34),
			new Among("\uFEB5", -1, 35), new Among("\uFEB6", -1, 35), new Among("\uFEB7", -1, 35),
			new Among("\uFEB8", -1, 35), new Among("\uFEB9", -1, 36), new Among("\uFEBA", -1, 36),
			new Among("\uFEBB", -1, 36), new Among("\uFEBC", -1, 36), new Among("\uFEBD", -1, 37),
			new Among("\uFEBE", -1, 37), new Among("\uFEBF", -1, 37), new Among("\uFEC0", -1, 37),
			new Among("\uFEC1", -1, 38), new Among("\uFEC2", -1, 38), new Among("\uFEC3", -1, 38),
			new Among("\uFEC4", -1, 38), new Among("\uFEC5", -1, 39), new Among("\uFEC6", -1, 39),
			new Among("\uFEC7", -1, 39), new Among("\uFEC8", -1, 39), new Among("\uFEC9", -1, 40),
			new Among("\uFECA", -1, 40), new Among("\uFECB", -1, 40), new Among("\uFECC", -1, 40),
			new Among("\uFECD", -1, 41), new Among("\uFECE", -1, 41), new Among("\uFECF", -1, 41),
			new Among("\uFED0", -1, 41), new Among("\uFED1", -1, 42), new Among("\uFED2", -1, 42),
			new Among("\uFED3", -1, 42), new Among("\uFED4", -1, 42), new Among("\uFED5", -1, 43),
			new Among("\uFED6", -1, 43), new Among("\uFED7", -1, 43), new Among("\uFED8", -1, 43),
			new Among("\uFED9", -1, 44), new Among("\uFEDA", -1, 44), new Among("\uFEDB", -1, 44),
			new Among("\uFEDC", -1, 44), new Among("\uFEDD", -1, 45), new Among("\uFEDE", -1, 45),
			new Among("\uFEDF", -1, 45), new Among("\uFEE0", -1, 45), new Among("\uFEE1", -1, 46),
			new Among("\uFEE2", -1, 46), new Among("\uFEE3", -1, 46), new Among("\uFEE4", -1, 46),
			new Among("\uFEE5", -1, 47), new Among("\uFEE6", -1, 47), new Among("\uFEE7", -1, 47),
			new Among("\uFEE8", -1, 47), new Among("\uFEE9", -1, 48), new Among("\uFEEA", -1, 48),
			new Among("\uFEEB", -1, 48), new Among("\uFEEC", -1, 48), new Among("\uFEED", -1, 49),
			new Among("\uFEEE", -1, 49), new Among("\uFEEF", -1, 50), new Among("\uFEF0", -1, 50),
			new Among("\uFEF1", -1, 51), new Among("\uFEF2", -1, 51), new Among("\uFEF3", -1, 51),
			new Among("\uFEF4", -1, 51), new Among("\uFEF5", -1, 55), new Among("\uFEF6", -1, 55),
			new Among("\uFEF7", -1, 53), new Among("\uFEF8", -1, 53), new Among("\uFEF9", -1, 54),
			new Among("\uFEFA", -1, 54), new Among("\uFEFB", -1, 52), new Among("\uFEFC", -1, 52) };

	private final static Among a_1[] = { new Among("\u0622", -1, 1), new Among("\u0623", -1, 1),
			new Among("\u0624", -1, 2), new Among("\u0625", -1, 1), new Among("\u0626", -1, 3) };

	private final static Among a_2[] = { new Among("\u0622", -1, 1), new Among("\u0623", -1, 1),
			new Among("\u0624", -1, 2), new Among("\u0625", -1, 1), new Among("\u0626", -1, 3) };

	private final static Among a_3[] = { new Among("\u0627\u0644", -1, 2), new Among("\u0628\u0627\u0644", -1, 1),
			new Among("\u0643\u0627\u0644", -1, 1), new Among("\u0644\u0644", -1, 2) };

	private final static Among a_4[] = { new Among("\u0623\u0622", -1, 2), new Among("\u0623\u0623", -1, 1),
			new Among("\u0623\u0624", -1, 3), new Among("\u0623\u0625", -1, 5), new Among("\u0623\u0627", -1, 4) };

	private final static Among a_5[] = { new Among("\u0641", -1, 1), new Among("\u0648", -1, 2) };

	private final static Among a_6[] = { new Among("\u0627\u0644", -1, 2), new Among("\u0628\u0627\u0644", -1, 1),
			new Among("\u0643\u0627\u0644", -1, 1), new Among("\u0644\u0644", -1, 2) };

	private final static Among a_7[] = { new Among("\u0628", -1, 1), new Among("\u0628\u0628", 0, 2),
			new Among("\u0643\u0643", -1, 3) };

	private final static Among a_8[] = { new Among("\u0633\u0623", -1, 4), new Among("\u0633\u062A", -1, 2),
			new Among("\u0633\u0646", -1, 3), new Among("\u0633\u064A", -1, 1) };

	private final static Among a_9[] = { new Among("\u062A\u0633\u062A", -1, 1), new Among("\u0646\u0633\u062A", -1, 1),
			new Among("\u064A\u0633\u062A", -1, 1) };

	private final static Among a_10[] = { new Among("\u0643\u0645\u0627", -1, 3),
			new Among("\u0647\u0645\u0627", -1, 3), new Among("\u0646\u0627", -1, 2), new Among("\u0647\u0627", -1, 2),
			new Among("\u0643", -1, 1), new Among("\u0643\u0645", -1, 2), new Among("\u0647\u0645", -1, 2),
			new Among("\u0647\u0646", -1, 2), new Among("\u0647", -1, 1), new Among("\u064A", -1, 1) };

	private final static Among a_11[] = { new Among("\u0646", -1, 1) };

	private final static Among a_12[] = { new Among("\u0627", -1, 1), new Among("\u0648", -1, 1),
			new Among("\u064A", -1, 1) };

	private final static Among a_13[] = { new Among("\u0627\u062A", -1, 1) };

	private final static Among a_14[] = { new Among("\u062A", -1, 1) };

	private final static Among a_15[] = { new Among("\u0629", -1, 1) };

	private final static Among a_16[] = { new Among("\u064A", -1, 1) };

	private final static Among a_17[] = { new Among("\u0643\u0645\u0627", -1, 3),
			new Among("\u0647\u0645\u0627", -1, 3), new Among("\u0646\u0627", -1, 2), new Among("\u0647\u0627", -1, 2),
			new Among("\u0643", -1, 1), new Among("\u0643\u0645", -1, 2), new Among("\u0647\u0645", -1, 2),
			new Among("\u0643\u0646", -1, 2), new Among("\u0647\u0646", -1, 2), new Among("\u0647", -1, 1),
			new Among("\u0643\u0645\u0648", -1, 3), new Among("\u0646\u064A", -1, 2) };

	private final static Among a_18[] = { new Among("\u0627", -1, 2), new Among("\u062A\u0627", 0, 3),
			new Among("\u062A\u0645\u0627", 0, 5), new Among("\u0646\u0627", 0, 3), new Among("\u062A", -1, 1),
			new Among("\u0646", -1, 2), new Among("\u0627\u0646", 5, 4), new Among("\u062A\u0646", 5, 3),
			new Among("\u0648\u0646", 5, 4), new Among("\u064A\u0646", 5, 4), new Among("\u064A", -1, 2) };

	private final static Among a_19[] = { new Among("\u0648\u0627", -1, 1), new Among("\u062A\u0645", -1, 1) };

	private final static Among a_20[] = { new Among("\u0648", -1, 1), new Among("\u062A\u0645\u0648", 0, 2) };

	private final static Among a_21[] = { new Among("\u0649", -1, 1) };

	private boolean B_is_defined;
	private boolean B_is_verb;
	private boolean B_is_noun;
	private int I_word_len;

	private boolean r_Normalize_pre() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 258
		// loop, line 259
		for (v_1 = (current.length()); v_1 > 0; v_1--) {
			// (, line 259
			// or, line 328
			lab0: do {
				v_2 = cursor;
				lab1: do {
					// (, line 260
					// [, line 261
					bra = cursor;
					// substring, line 261
					among_var = find_among(a_0);
					if (among_var == 0) {
						break lab1;
					}
					// ], line 261
					ket = cursor;
					switch (among_var) {
					case 0:
						break lab1;
					case 1:
						// (, line 262
						// delete, line 262
						slice_del();
						break;
					case 2:
						// (, line 263
						// delete, line 263
						slice_del();
						break;
					case 3:
						// (, line 266
						// delete, line 266
						slice_del();
						break;
					case 4:
						// (, line 267
						// delete, line 267
						slice_del();
						break;
					case 5:
						// (, line 270
						// <-, line 270
						slice_from("0");
						break;
					case 6:
						// (, line 271
						// <-, line 271
						slice_from("1");
						break;
					case 7:
						// (, line 272
						// <-, line 272
						slice_from("2");
						break;
					case 8:
						// (, line 273
						// <-, line 273
						slice_from("3");
						break;
					case 9:
						// (, line 274
						// <-, line 274
						slice_from("4");
						break;
					case 10:
						// (, line 275
						// <-, line 275
						slice_from("5");
						break;
					case 11:
						// (, line 276
						// <-, line 276
						slice_from("6");
						break;
					case 12:
						// (, line 277
						// <-, line 277
						slice_from("7");
						break;
					case 13:
						// (, line 278
						// <-, line 278
						slice_from("8");
						break;
					case 14:
						// (, line 279
						// <-, line 279
						slice_from("9");
						break;
					case 15:
						// (, line 280
						// delete, line 280
						slice_del();
						break;
					case 16:
						// (, line 283
						// <-, line 283
						slice_from("\u0621");
						break;
					case 17:
						// (, line 284
						// <-, line 284
						slice_from("\u0623");
						break;
					case 18:
						// (, line 285
						// <-, line 285
						slice_from("\u0625");
						break;
					case 19:
						// (, line 286
						// <-, line 286
						slice_from("\u0626");
						break;
					case 20:
						// (, line 287
						// <-, line 287
						slice_from("\u0622");
						break;
					case 21:
						// (, line 288
						// <-, line 288
						slice_from("\u0624");
						break;
					case 22:
						// (, line 289
						// <-, line 289
						slice_from("\u0627");
						break;
					case 23:
						// (, line 290
						// <-, line 290
						slice_from("\u0628");
						break;
					case 24:
						// (, line 291
						// <-, line 291
						slice_from("\u0629");
						break;
					case 25:
						// (, line 292
						// <-, line 292
						slice_from("\u062A");
						break;
					case 26:
						// (, line 293
						// <-, line 293
						slice_from("\u062B");
						break;
					case 27:
						// (, line 294
						// <-, line 294
						slice_from("\u062C");
						break;
					case 28:
						// (, line 295
						// <-, line 295
						slice_from("\u062D");
						break;
					case 29:
						// (, line 296
						// <-, line 296
						slice_from("\u062E");
						break;
					case 30:
						// (, line 297
						// <-, line 297
						slice_from("\u062F");
						break;
					case 31:
						// (, line 298
						// <-, line 298
						slice_from("\u0630");
						break;
					case 32:
						// (, line 299
						// <-, line 299
						slice_from("\u0631");
						break;
					case 33:
						// (, line 300
						// <-, line 300
						slice_from("\u0632");
						break;
					case 34:
						// (, line 301
						// <-, line 301
						slice_from("\u0633");
						break;
					case 35:
						// (, line 302
						// <-, line 302
						slice_from("\u0634");
						break;
					case 36:
						// (, line 303
						// <-, line 303
						slice_from("\u0635");
						break;
					case 37:
						// (, line 304
						// <-, line 304
						slice_from("\u0636");
						break;
					case 38:
						// (, line 305
						// <-, line 305
						slice_from("\u0637");
						break;
					case 39:
						// (, line 306
						// <-, line 306
						slice_from("\u0638");
						break;
					case 40:
						// (, line 307
						// <-, line 307
						slice_from("\u0639");
						break;
					case 41:
						// (, line 308
						// <-, line 308
						slice_from("\u063A");
						break;
					case 42:
						// (, line 309
						// <-, line 309
						slice_from("\u0641");
						break;
					case 43:
						// (, line 310
						// <-, line 310
						slice_from("\u0642");
						break;
					case 44:
						// (, line 311
						// <-, line 311
						slice_from("\u0643");
						break;
					case 45:
						// (, line 312
						// <-, line 312
						slice_from("\u0644");
						break;
					case 46:
						// (, line 313
						// <-, line 313
						slice_from("\u0645");
						break;
					case 47:
						// (, line 314
						// <-, line 314
						slice_from("\u0646");
						break;
					case 48:
						// (, line 315
						// <-, line 315
						slice_from("\u0647");
						break;
					case 49:
						// (, line 316
						// <-, line 316
						slice_from("\u0648");
						break;
					case 50:
						// (, line 317
						// <-, line 317
						slice_from("\u0649");
						break;
					case 51:
						// (, line 318
						// <-, line 318
						slice_from("\u064A");
						break;
					case 52:
						// (, line 321
						// <-, line 321
						slice_from("\u0644\u0627");
						break;
					case 53:
						// (, line 322
						// <-, line 322
						slice_from("\u0644\u0623");
						break;
					case 54:
						// (, line 323
						// <-, line 323
						slice_from("\u0644\u0625");
						break;
					case 55:
						// (, line 324
						// <-, line 324
						slice_from("\u0644\u0622");
						break;
					}
					break lab0;
				} while (false);
				cursor = v_2;
				// next, line 329
				if (cursor >= limit) {
					return false;
				}
				cursor++;
			} while (false);
		}
		return true;
	}

	private boolean r_Normalize_post() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 333
		// do, line 335
		v_1 = cursor;
		lab0: do {
			// (, line 335
			// backwards, line 337
			limit_backward = cursor;
			cursor = limit;
			// (, line 337
			// [, line 338
			ket = cursor;
			// substring, line 338
			among_var = find_among_b(a_1);
			if (among_var == 0) {
				break lab0;
			}
			// ], line 338
			bra = cursor;
			switch (among_var) {
			case 0:
				break lab0;
			case 1:
				// (, line 339
				// <-, line 339
				slice_from("\u0621");
				break;
			case 2:
				// (, line 340
				// <-, line 340
				slice_from("\u0621");
				break;
			case 3:
				// (, line 341
				// <-, line 341
				slice_from("\u0621");
				break;
			}
			cursor = limit_backward;
		} while (false);
		cursor = v_1;
		// do, line 346
		v_2 = cursor;
		lab1: do {
			// loop, line 346
			for (v_3 = I_word_len; v_3 > 0; v_3--) {
				// (, line 346
				// or, line 355
				lab2: do {
					v_4 = cursor;
					lab3: do {
						// (, line 347
						// [, line 349
						bra = cursor;
						// substring, line 349
						among_var = find_among(a_2);
						if (among_var == 0) {
							break lab3;
						}
						// ], line 349
						ket = cursor;
						switch (among_var) {
						case 0:
							break lab3;
						case 1:
							// (, line 350
							// <-, line 350
							slice_from("\u0627");
							break;
						case 2:
							// (, line 351
							// <-, line 351
							slice_from("\u0648");
							break;
						case 3:
							// (, line 352
							// <-, line 352
							slice_from("\u064A");
							break;
						}
						break lab2;
					} while (false);
					cursor = v_4;
					// next, line 356
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				} while (false);
			}
		} while (false);
		cursor = v_2;
		return true;
	}

	private boolean r_Checks1() {
		int among_var;
		// (, line 361
		I_word_len = (current.length());
		// [, line 363
		bra = cursor;
		// substring, line 363
		among_var = find_among(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 363
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 364
			if (!(I_word_len > 4)) {
				return false;
			}
			// set is_noun, line 364
			B_is_noun = true;
			// unset is_verb, line 364
			B_is_verb = false;
			// set is_defined, line 364
			B_is_defined = true;
			break;
		case 2:
			// (, line 365
			if (!(I_word_len > 3)) {
				return false;
			}
			// set is_noun, line 365
			B_is_noun = true;
			// unset is_verb, line 365
			B_is_verb = false;
			// set is_defined, line 365
			B_is_defined = true;
			break;
		}
		return true;
	}

	private boolean r_Prefix_Step1() {
		int among_var;
		// (, line 371
		I_word_len = (current.length());
		// [, line 373
		bra = cursor;
		// substring, line 373
		among_var = find_among(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 373
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 374
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 374
			slice_from("\u0623");
			break;
		case 2:
			// (, line 375
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 375
			slice_from("\u0622");
			break;
		case 3:
			// (, line 376
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 376
			slice_from("\u0623");
			break;
		case 4:
			// (, line 377
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 377
			slice_from("\u0627");
			break;
		case 5:
			// (, line 378
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 378
			slice_from("\u0625");
			break;
		}
		return true;
	}

	private boolean r_Prefix_Step2() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 383
		I_word_len = (current.length());
		// not, line 385
		{
			v_1 = cursor;
			lab0: do {
				// literal, line 385
				if (!(eq_s("\u0641\u0627"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = v_1;
		}
		// not, line 386
		{
			v_2 = cursor;
			lab1: do {
				// literal, line 386
				if (!(eq_s("\u0648\u0627"))) {
					break lab1;
				}
				return false;
			} while (false);
			cursor = v_2;
		}
		// [, line 387
		bra = cursor;
		// substring, line 387
		among_var = find_among(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 387
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 388
			if (!(I_word_len > 3)) {
				return false;
			}
			// delete, line 388
			slice_del();
			break;
		case 2:
			// (, line 389
			if (!(I_word_len > 3)) {
				return false;
			}
			// delete, line 389
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Prefix_Step3a_Noun() {
		int among_var;
		// (, line 393
		I_word_len = (current.length());
		// [, line 395
		bra = cursor;
		// substring, line 395
		among_var = find_among(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 395
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 396
			if (!(I_word_len > 5)) {
				return false;
			}
			// delete, line 396
			slice_del();
			break;
		case 2:
			// (, line 397
			if (!(I_word_len > 4)) {
				return false;
			}
			// delete, line 397
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Prefix_Step3b_Noun() {
		int among_var;
		int v_1;
		// (, line 401
		I_word_len = (current.length());
		// not, line 403
		{
			v_1 = cursor;
			lab0: do {
				// literal, line 403
				if (!(eq_s("\u0628\u0627"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = v_1;
		}
		// [, line 404
		bra = cursor;
		// substring, line 404
		among_var = find_among(a_7);
		if (among_var == 0) {
			return false;
		}
		// ], line 404
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 405
			if (!(I_word_len > 3)) {
				return false;
			}
			// delete, line 405
			slice_del();
			break;
		case 2:
			// (, line 407
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 407
			slice_from("\u0628");
			break;
		case 3:
			// (, line 408
			if (!(I_word_len > 3)) {
				return false;
			}
			// <-, line 408
			slice_from("\u0643");
			break;
		}
		return true;
	}

	private boolean r_Prefix_Step3_Verb() {
		int among_var;
		// (, line 413
		I_word_len = (current.length());
		// [, line 415
		bra = cursor;
		// substring, line 415
		among_var = find_among(a_8);
		if (among_var == 0) {
			return false;
		}
		// ], line 415
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 417
			if (!(I_word_len > 4)) {
				return false;
			}
			// <-, line 417
			slice_from("\u064A");
			break;
		case 2:
			// (, line 418
			if (!(I_word_len > 4)) {
				return false;
			}
			// <-, line 418
			slice_from("\u062A");
			break;
		case 3:
			// (, line 419
			if (!(I_word_len > 4)) {
				return false;
			}
			// <-, line 419
			slice_from("\u0646");
			break;
		case 4:
			// (, line 420
			if (!(I_word_len > 4)) {
				return false;
			}
			// <-, line 420
			slice_from("\u0623");
			break;
		}
		return true;
	}

	private boolean r_Prefix_Step4_Verb() {
		int among_var;
		// (, line 424
		I_word_len = (current.length());
		// [, line 426
		bra = cursor;
		// substring, line 426
		among_var = find_among(a_9);
		if (among_var == 0) {
			return false;
		}
		// ], line 426
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 427
			if (!(I_word_len > 4)) {
				return false;
			}
			// set is_verb, line 427
			B_is_verb = true;
			// unset is_noun, line 427
			B_is_noun = false;
			// <-, line 427
			slice_from("\u0627\u0633\u062A");
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step1a() {
		int among_var;
		// (, line 434
		I_word_len = (current.length());
		// [, line 436
		ket = cursor;
		// substring, line 436
		among_var = find_among_b(a_10);
		if (among_var == 0) {
			return false;
		}
		// ], line 436
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 437
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 437
			slice_del();
			break;
		case 2:
			// (, line 438
			if (!(I_word_len >= 5)) {
				return false;
			}
			// delete, line 438
			slice_del();
			break;
		case 3:
			// (, line 439
			if (!(I_word_len >= 6)) {
				return false;
			}
			// delete, line 439
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step1b() {
		int among_var;
		// (, line 442
		I_word_len = (current.length());
		// [, line 444
		ket = cursor;
		// substring, line 444
		among_var = find_among_b(a_11);
		if (among_var == 0) {
			return false;
		}
		// ], line 444
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 445
			if (!(I_word_len > 5)) {
				return false;
			}
			// delete, line 445
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step2a() {
		int among_var;
		// (, line 449
		I_word_len = (current.length());
		// [, line 451
		ket = cursor;
		// substring, line 451
		among_var = find_among_b(a_12);
		if (among_var == 0) {
			return false;
		}
		// ], line 451
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 452
			if (!(I_word_len > 4)) {
				return false;
			}
			// delete, line 452
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step2b() {
		int among_var;
		// (, line 456
		I_word_len = (current.length());
		// [, line 458
		ket = cursor;
		// substring, line 458
		among_var = find_among_b(a_13);
		if (among_var == 0) {
			return false;
		}
		// ], line 458
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 459
			if (!(I_word_len >= 5)) {
				return false;
			}
			// delete, line 459
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step2c1() {
		int among_var;
		// (, line 463
		I_word_len = (current.length());
		// [, line 465
		ket = cursor;
		// substring, line 465
		among_var = find_among_b(a_14);
		if (among_var == 0) {
			return false;
		}
		// ], line 465
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 466
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 466
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step2c2() {
		int among_var;
		// (, line 469
		I_word_len = (current.length());
		// [, line 471
		ket = cursor;
		// substring, line 471
		among_var = find_among_b(a_15);
		if (among_var == 0) {
			return false;
		}
		// ], line 471
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 472
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 472
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Noun_Step3() {
		int among_var;
		// (, line 475
		I_word_len = (current.length());
		// [, line 477
		ket = cursor;
		// substring, line 477
		among_var = find_among_b(a_16);
		if (among_var == 0) {
			return false;
		}
		// ], line 477
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 478
			if (!(I_word_len >= 3)) {
				return false;
			}
			// delete, line 478
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Verb_Step1() {
		int among_var;
		// (, line 482
		I_word_len = (current.length());
		// [, line 484
		ket = cursor;
		// substring, line 484
		among_var = find_among_b(a_17);
		if (among_var == 0) {
			return false;
		}
		// ], line 484
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 485
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 485
			slice_del();
			break;
		case 2:
			// (, line 486
			if (!(I_word_len >= 5)) {
				return false;
			}
			// delete, line 486
			slice_del();
			break;
		case 3:
			// (, line 487
			if (!(I_word_len >= 6)) {
				return false;
			}
			// delete, line 487
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Verb_Step2a() {
		int among_var;
		// (, line 490
		I_word_len = (current.length());
		// [, line 492
		ket = cursor;
		// substring, line 492
		among_var = find_among_b(a_18);
		if (among_var == 0) {
			return false;
		}
		// ], line 492
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 493
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 493
			slice_del();
			break;
		case 2:
			// (, line 494
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 494
			slice_del();
			break;
		case 3:
			// (, line 495
			if (!(I_word_len >= 5)) {
				return false;
			}
			// delete, line 495
			slice_del();
			break;
		case 4:
			// (, line 496
			if (!(I_word_len > 5)) {
				return false;
			}
			// delete, line 496
			slice_del();
			break;
		case 5:
			// (, line 497
			if (!(I_word_len >= 6)) {
				return false;
			}
			// delete, line 497
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Verb_Step2b() {
		int among_var;
		// (, line 501
		I_word_len = (current.length());
		// [, line 503
		ket = cursor;
		// substring, line 503
		among_var = find_among_b(a_19);
		if (among_var == 0) {
			return false;
		}
		// ], line 503
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 504
			if (!(I_word_len >= 5)) {
				return false;
			}
			// delete, line 504
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_Verb_Step2c() {
		int among_var;
		// (, line 509
		I_word_len = (current.length());
		// [, line 511
		ket = cursor;
		// substring, line 511
		among_var = find_among_b(a_20);
		if (among_var == 0) {
			return false;
		}
		// ], line 511
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 512
			if (!(I_word_len >= 4)) {
				return false;
			}
			// delete, line 512
			slice_del();
			break;
		case 2:
			// (, line 513
			if (!(I_word_len >= 6)) {
				return false;
			}
			// delete, line 513
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Suffix_All_alef_maqsura() {
		int among_var;
		// (, line 517
		I_word_len = (current.length());
		// [, line 519
		ket = cursor;
		// substring, line 519
		among_var = find_among_b(a_21);
		if (among_var == 0) {
			return false;
		}
		// ], line 519
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 520
			// <-, line 520
			slice_from("\u064A");
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_12;
		int v_13;
		int v_15;
		int v_16;
		int v_17;
		int v_18;
		int v_19;
		int v_20;
		// (, line 527
		// set is_noun, line 529
		B_is_noun = true;
		// set is_verb, line 530
		B_is_verb = true;
		// unset is_defined, line 531
		B_is_defined = false;
		// do, line 534
		v_1 = cursor;
		lab0: do {
			// call Checks1, line 534
			if (!r_Checks1()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 537
		v_2 = cursor;
		lab1: do {
			// call Normalize_pre, line 537
			if (!r_Normalize_pre()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 540
		limit_backward = cursor;
		cursor = limit;
		// (, line 540
		// do, line 542
		v_3 = limit - cursor;
		lab2: do {
			// (, line 542
			// or, line 556
			lab3: do {
				v_4 = limit - cursor;
				lab4: do {
					// (, line 544
					// Boolean test is_verb, line 545
					if (!(B_is_verb)) {
						break lab4;
					}
					// (, line 546
					// or, line 551
					lab5: do {
						v_5 = limit - cursor;
						lab6: do {
							// (, line 547
							// (, line 548
							// atleast, line 548
							{
								int v_6 = 1;
								// atleast, line 548
								replab7: while (true) {
									v_7 = limit - cursor;
									lab8: do {
										// call Suffix_Verb_Step1, line 548
										if (!r_Suffix_Verb_Step1()) {
											break lab8;
										}
										v_6--;
										continue replab7;
									} while (false);
									cursor = limit - v_7;
									break replab7;
								}
								if (v_6 > 0) {
									break lab6;
								}
							}
							// (, line 549
							// or, line 549
							lab9: do {
								v_8 = limit - cursor;
								lab10: do {
									// call Suffix_Verb_Step2a, line 549
									if (!r_Suffix_Verb_Step2a()) {
										break lab10;
									}
									break lab9;
								} while (false);
								cursor = limit - v_8;
								lab11: do {
									// call Suffix_Verb_Step2c, line 549
									if (!r_Suffix_Verb_Step2c()) {
										break lab11;
									}
									break lab9;
								} while (false);
								cursor = limit - v_8;
								// next, line 549
								if (cursor <= limit_backward) {
									break lab6;
								}
								cursor--;
							} while (false);
							break lab5;
						} while (false);
						cursor = limit - v_5;
						lab12: do {
							// call Suffix_Verb_Step2b, line 551
							if (!r_Suffix_Verb_Step2b()) {
								break lab12;
							}
							break lab5;
						} while (false);
						cursor = limit - v_5;
						// call Suffix_Verb_Step2a, line 552
						if (!r_Suffix_Verb_Step2a()) {
							break lab4;
						}
					} while (false);
					break lab3;
				} while (false);
				cursor = limit - v_4;
				lab13: do {
					// (, line 556
					// Boolean test is_noun, line 557
					if (!(B_is_noun)) {
						break lab13;
					}
					// (, line 558
					// try, line 560
					v_9 = limit - cursor;
					lab14: do {
						// (, line 560
						// or, line 562
						lab15: do {
							v_10 = limit - cursor;
							lab16: do {
								// call Suffix_Noun_Step2c2, line 561
								if (!r_Suffix_Noun_Step2c2()) {
									break lab16;
								}
								break lab15;
							} while (false);
							cursor = limit - v_10;
							lab17: do {
								// (, line 562
								// not, line 562
								lab18: do {
									// Boolean test is_defined, line 562
									if (!(B_is_defined)) {
										break lab18;
									}
									break lab17;
								} while (false);
								// call Suffix_Noun_Step1a, line 562
								if (!r_Suffix_Noun_Step1a()) {
									break lab17;
								}
								// (, line 562
								// or, line 564
								lab19: do {
									v_12 = limit - cursor;
									lab20: do {
										// call Suffix_Noun_Step2a, line 563
										if (!r_Suffix_Noun_Step2a()) {
											break lab20;
										}
										break lab19;
									} while (false);
									cursor = limit - v_12;
									lab21: do {
										// call Suffix_Noun_Step2b, line 564
										if (!r_Suffix_Noun_Step2b()) {
											break lab21;
										}
										break lab19;
									} while (false);
									cursor = limit - v_12;
									lab22: do {
										// call Suffix_Noun_Step2c1, line 565
										if (!r_Suffix_Noun_Step2c1()) {
											break lab22;
										}
										break lab19;
									} while (false);
									cursor = limit - v_12;
									// next, line 566
									if (cursor <= limit_backward) {
										break lab17;
									}
									cursor--;
								} while (false);
								break lab15;
							} while (false);
							cursor = limit - v_10;
							lab23: do {
								// (, line 567
								// call Suffix_Noun_Step1b, line 567
								if (!r_Suffix_Noun_Step1b()) {
									break lab23;
								}
								// (, line 567
								// or, line 569
								lab24: do {
									v_13 = limit - cursor;
									lab25: do {
										// call Suffix_Noun_Step2a, line 568
										if (!r_Suffix_Noun_Step2a()) {
											break lab25;
										}
										break lab24;
									} while (false);
									cursor = limit - v_13;
									lab26: do {
										// call Suffix_Noun_Step2b, line 569
										if (!r_Suffix_Noun_Step2b()) {
											break lab26;
										}
										break lab24;
									} while (false);
									cursor = limit - v_13;
									// call Suffix_Noun_Step2c1, line 570
									if (!r_Suffix_Noun_Step2c1()) {
										break lab23;
									}
								} while (false);
								break lab15;
							} while (false);
							cursor = limit - v_10;
							lab27: do {
								// (, line 571
								// not, line 571
								lab28: do {
									// Boolean test is_defined, line 571
									if (!(B_is_defined)) {
										break lab28;
									}
									break lab27;
								} while (false);
								// call Suffix_Noun_Step2a, line 571
								if (!r_Suffix_Noun_Step2a()) {
									break lab27;
								}
								break lab15;
							} while (false);
							cursor = limit - v_10;
							// (, line 572
							// call Suffix_Noun_Step2b, line 572
							if (!r_Suffix_Noun_Step2b()) {
								cursor = limit - v_9;
								break lab14;
							}
						} while (false);
					} while (false);
					// call Suffix_Noun_Step3, line 574
					if (!r_Suffix_Noun_Step3()) {
						break lab13;
					}
					break lab3;
				} while (false);
				cursor = limit - v_4;
				// call Suffix_All_alef_maqsura, line 580
				if (!r_Suffix_All_alef_maqsura()) {
					break lab2;
				}
			} while (false);
		} while (false);
		cursor = limit - v_3;
		cursor = limit_backward; // do, line 585
		v_15 = cursor;
		lab29: do {
			// (, line 585
			// try, line 586
			v_16 = cursor;
			lab30: do {
				// call Prefix_Step1, line 586
				if (!r_Prefix_Step1()) {
					cursor = v_16;
					break lab30;
				}
			} while (false);
			// try, line 587
			v_17 = cursor;
			lab31: do {
				// call Prefix_Step2, line 587
				if (!r_Prefix_Step2()) {
					cursor = v_17;
					break lab31;
				}
			} while (false);
			// (, line 588
			// or, line 589
			lab32: do {
				v_18 = cursor;
				lab33: do {
					// call Prefix_Step3a_Noun, line 588
					if (!r_Prefix_Step3a_Noun()) {
						break lab33;
					}
					break lab32;
				} while (false);
				cursor = v_18;
				lab34: do {
					// (, line 589
					// Boolean test is_noun, line 589
					if (!(B_is_noun)) {
						break lab34;
					}
					// call Prefix_Step3b_Noun, line 589
					if (!r_Prefix_Step3b_Noun()) {
						break lab34;
					}
					break lab32;
				} while (false);
				cursor = v_18;
				// (, line 590
				// Boolean test is_verb, line 590
				if (!(B_is_verb)) {
					break lab29;
				}
				// try, line 590
				v_19 = cursor;
				lab35: do {
					// call Prefix_Step3_Verb, line 590
					if (!r_Prefix_Step3_Verb()) {
						cursor = v_19;
						break lab35;
					}
				} while (false);
				// call Prefix_Step4_Verb, line 590
				if (!r_Prefix_Step4_Verb()) {
					break lab29;
				}
			} while (false);
		} while (false);
		cursor = v_15;
		// do, line 595
		v_20 = cursor;
		lab36: do {
			// call Normalize_post, line 595
			if (!r_Normalize_post()) {
				break lab36;
			}
		} while (false);
		cursor = v_20;
		return true;
	}
}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class armenianStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("\u0580\u0578\u0580\u0564", -1, 1),
			new Among("\u0565\u0580\u0578\u0580\u0564", 0, 1), new Among("\u0561\u056C\u056B", -1, 1),
			new Among("\u0561\u056F\u056B", -1, 1), new Among("\u0578\u0580\u0561\u056F", -1, 1),
			new Among("\u0565\u0572", -1, 1), new Among("\u0561\u056F\u0561\u0576", -1, 1),
			new Among("\u0561\u0580\u0561\u0576", -1, 1), new Among("\u0565\u0576", -1, 1),
			new Among("\u0565\u056F\u0565\u0576", 8, 1), new Among("\u0565\u0580\u0565\u0576", 8, 1),
			new Among("\u0578\u0580\u0567\u0576", -1, 1), new Among("\u056B\u0576", -1, 1),
			new Among("\u0563\u056B\u0576", 12, 1), new Among("\u0578\u057E\u056B\u0576", 12, 1),
			new Among("\u056C\u0561\u0575\u0576", -1, 1), new Among("\u057E\u0578\u0582\u0576", -1, 1),
			new Among("\u057A\u0565\u057D", -1, 1), new Among("\u056B\u057E", -1, 1), new Among("\u0561\u057F", -1, 1),
			new Among("\u0561\u057E\u0565\u057F", -1, 1), new Among("\u056F\u0578\u057F", -1, 1),
			new Among("\u0562\u0561\u0580", -1, 1) };

	private final static Among a_1[] = { new Among("\u0561", -1, 1), new Among("\u0561\u0581\u0561", 0, 1),
			new Among("\u0565\u0581\u0561", 0, 1), new Among("\u057E\u0565", -1, 1),
			new Among("\u0561\u0581\u0580\u056B", -1, 1), new Among("\u0561\u0581\u056B", -1, 1),
			new Among("\u0565\u0581\u056B", -1, 1), new Among("\u057E\u0565\u0581\u056B", 6, 1),
			new Among("\u0561\u056C", -1, 1), new Among("\u0568\u0561\u056C", 8, 1),
			new Among("\u0561\u0576\u0561\u056C", 8, 1), new Among("\u0565\u0576\u0561\u056C", 8, 1),
			new Among("\u0561\u0581\u0576\u0561\u056C", 8, 1), new Among("\u0565\u056C", -1, 1),
			new Among("\u0568\u0565\u056C", 13, 1), new Among("\u0576\u0565\u056C", 13, 1),
			new Among("\u0581\u0576\u0565\u056C", 15, 1), new Among("\u0565\u0581\u0576\u0565\u056C", 16, 1),
			new Among("\u0579\u0565\u056C", 13, 1), new Among("\u057E\u0565\u056C", 13, 1),
			new Among("\u0561\u0581\u057E\u0565\u056C", 19, 1), new Among("\u0565\u0581\u057E\u0565\u056C", 19, 1),
			new Among("\u057F\u0565\u056C", 13, 1), new Among("\u0561\u057F\u0565\u056C", 22, 1),
			new Among("\u0578\u057F\u0565\u056C", 22, 1), new Among("\u056F\u0578\u057F\u0565\u056C", 24, 1),
			new Among("\u057E\u0561\u056E", -1, 1), new Among("\u0578\u0582\u0574", -1, 1),
			new Among("\u057E\u0578\u0582\u0574", 27, 1), new Among("\u0561\u0576", -1, 1),
			new Among("\u0581\u0561\u0576", 29, 1), new Among("\u0561\u0581\u0561\u0576", 30, 1),
			new Among("\u0561\u0581\u0580\u056B\u0576", -1, 1), new Among("\u0561\u0581\u056B\u0576", -1, 1),
			new Among("\u0565\u0581\u056B\u0576", -1, 1), new Among("\u057E\u0565\u0581\u056B\u0576", 34, 1),
			new Among("\u0561\u056C\u056B\u057D", -1, 1), new Among("\u0565\u056C\u056B\u057D", -1, 1),
			new Among("\u0561\u057E", -1, 1), new Among("\u0561\u0581\u0561\u057E", 38, 1),
			new Among("\u0565\u0581\u0561\u057E", 38, 1), new Among("\u0561\u056C\u0578\u057E", -1, 1),
			new Among("\u0565\u056C\u0578\u057E", -1, 1), new Among("\u0561\u0580", -1, 1),
			new Among("\u0561\u0581\u0561\u0580", 43, 1), new Among("\u0565\u0581\u0561\u0580", 43, 1),
			new Among("\u0561\u0581\u0580\u056B\u0580", -1, 1), new Among("\u0561\u0581\u056B\u0580", -1, 1),
			new Among("\u0565\u0581\u056B\u0580", -1, 1), new Among("\u057E\u0565\u0581\u056B\u0580", 48, 1),
			new Among("\u0561\u0581", -1, 1), new Among("\u0565\u0581", -1, 1),
			new Among("\u0561\u0581\u0580\u0565\u0581", 51, 1), new Among("\u0561\u056C\u0578\u0582\u0581", -1, 1),
			new Among("\u0565\u056C\u0578\u0582\u0581", -1, 1), new Among("\u0561\u056C\u0578\u0582", -1, 1),
			new Among("\u0565\u056C\u0578\u0582", -1, 1), new Among("\u0561\u0584", -1, 1),
			new Among("\u0581\u0561\u0584", 57, 1), new Among("\u0561\u0581\u0561\u0584", 58, 1),
			new Among("\u0561\u0581\u0580\u056B\u0584", -1, 1), new Among("\u0561\u0581\u056B\u0584", -1, 1),
			new Among("\u0565\u0581\u056B\u0584", -1, 1), new Among("\u057E\u0565\u0581\u056B\u0584", 62, 1),
			new Among("\u0561\u0576\u0584", -1, 1), new Among("\u0581\u0561\u0576\u0584", 64, 1),
			new Among("\u0561\u0581\u0561\u0576\u0584", 65, 1),
			new Among("\u0561\u0581\u0580\u056B\u0576\u0584", -1, 1),
			new Among("\u0561\u0581\u056B\u0576\u0584", -1, 1), new Among("\u0565\u0581\u056B\u0576\u0584", -1, 1),
			new Among("\u057E\u0565\u0581\u056B\u0576\u0584", 69, 1) };

	private final static Among a_2[] = { new Among("\u0578\u0580\u0564", -1, 1),
			new Among("\u0578\u0582\u0575\u0569", -1, 1), new Among("\u0578\u0582\u0570\u056B", -1, 1),
			new Among("\u0581\u056B", -1, 1), new Among("\u056B\u056C", -1, 1), new Among("\u0561\u056F", -1, 1),
			new Among("\u0575\u0561\u056F", 5, 1), new Among("\u0561\u0576\u0561\u056F", 5, 1),
			new Among("\u056B\u056F", -1, 1), new Among("\u0578\u0582\u056F", -1, 1), new Among("\u0561\u0576", -1, 1),
			new Among("\u057A\u0561\u0576", 10, 1), new Among("\u057D\u057F\u0561\u0576", 10, 1),
			new Among("\u0561\u0580\u0561\u0576", 10, 1), new Among("\u0565\u0572\u0567\u0576", -1, 1),
			new Among("\u0575\u0578\u0582\u0576", -1, 1),
			new Among("\u0578\u0582\u0569\u0575\u0578\u0582\u0576", 15, 1), new Among("\u0561\u056E\u0578", -1, 1),
			new Among("\u056B\u0579", -1, 1), new Among("\u0578\u0582\u057D", -1, 1),
			new Among("\u0578\u0582\u057D\u057F", -1, 1), new Among("\u0563\u0561\u0580", -1, 1),
			new Among("\u057E\u0578\u0580", -1, 1), new Among("\u0561\u057E\u0578\u0580", 22, 1),
			new Among("\u0578\u0581", -1, 1), new Among("\u0561\u0576\u0585\u0581", -1, 1),
			new Among("\u0578\u0582", -1, 1), new Among("\u0584", -1, 1), new Among("\u0579\u0565\u0584", 27, 1),
			new Among("\u056B\u0584", 27, 1), new Among("\u0561\u056C\u056B\u0584", 29, 1),
			new Among("\u0561\u0576\u056B\u0584", 29, 1), new Among("\u057E\u0561\u056E\u0584", 27, 1),
			new Among("\u0578\u0582\u0575\u0584", 27, 1), new Among("\u0565\u0576\u0584", 27, 1),
			new Among("\u0578\u0576\u0584", 27, 1), new Among("\u0578\u0582\u0576\u0584", 27, 1),
			new Among("\u0574\u0578\u0582\u0576\u0584", 36, 1), new Among("\u056B\u0579\u0584", 27, 1),
			new Among("\u0561\u0580\u0584", 27, 1) };

	private final static Among a_3[] = { new Among("\u057D\u0561", -1, 1), new Among("\u057E\u0561", -1, 1),
			new Among("\u0561\u0574\u0562", -1, 1), new Among("\u0564", -1, 1), new Among("\u0561\u0576\u0564", 3, 1),
			new Among("\u0578\u0582\u0569\u0575\u0561\u0576\u0564", 4, 1), new Among("\u057E\u0561\u0576\u0564", 4, 1),
			new Among("\u0578\u057B\u0564", 3, 1), new Among("\u0565\u0580\u0564", 3, 1),
			new Among("\u0576\u0565\u0580\u0564", 8, 1), new Among("\u0578\u0582\u0564", 3, 1),
			new Among("\u0568", -1, 1), new Among("\u0561\u0576\u0568", 11, 1),
			new Among("\u0578\u0582\u0569\u0575\u0561\u0576\u0568", 12, 1),
			new Among("\u057E\u0561\u0576\u0568", 12, 1), new Among("\u0578\u057B\u0568", 11, 1),
			new Among("\u0565\u0580\u0568", 11, 1), new Among("\u0576\u0565\u0580\u0568", 16, 1),
			new Among("\u056B", -1, 1), new Among("\u057E\u056B", 18, 1), new Among("\u0565\u0580\u056B", 18, 1),
			new Among("\u0576\u0565\u0580\u056B", 20, 1), new Among("\u0561\u0576\u0578\u0582\u0574", -1, 1),
			new Among("\u0565\u0580\u0578\u0582\u0574", -1, 1),
			new Among("\u0576\u0565\u0580\u0578\u0582\u0574", 23, 1), new Among("\u0576", -1, 1),
			new Among("\u0561\u0576", 25, 1), new Among("\u0578\u0582\u0569\u0575\u0561\u0576", 26, 1),
			new Among("\u057E\u0561\u0576", 26, 1), new Among("\u056B\u0576", 25, 1),
			new Among("\u0565\u0580\u056B\u0576", 29, 1), new Among("\u0576\u0565\u0580\u056B\u0576", 30, 1),
			new Among("\u0578\u0582\u0569\u0575\u0561\u0576\u0576", 25, 1), new Among("\u0565\u0580\u0576", 25, 1),
			new Among("\u0576\u0565\u0580\u0576", 33, 1), new Among("\u0578\u0582\u0576", 25, 1),
			new Among("\u0578\u057B", -1, 1), new Among("\u0578\u0582\u0569\u0575\u0561\u0576\u057D", -1, 1),
			new Among("\u057E\u0561\u0576\u057D", -1, 1), new Among("\u0578\u057B\u057D", -1, 1),
			new Among("\u0578\u057E", -1, 1), new Among("\u0561\u0576\u0578\u057E", 40, 1),
			new Among("\u057E\u0578\u057E", 40, 1), new Among("\u0565\u0580\u0578\u057E", 40, 1),
			new Among("\u0576\u0565\u0580\u0578\u057E", 43, 1), new Among("\u0565\u0580", -1, 1),
			new Among("\u0576\u0565\u0580", 45, 1), new Among("\u0581", -1, 1), new Among("\u056B\u0581", 47, 1),
			new Among("\u057E\u0561\u0576\u056B\u0581", 48, 1), new Among("\u0578\u057B\u056B\u0581", 48, 1),
			new Among("\u057E\u056B\u0581", 48, 1), new Among("\u0565\u0580\u056B\u0581", 48, 1),
			new Among("\u0576\u0565\u0580\u056B\u0581", 52, 1), new Among("\u0581\u056B\u0581", 48, 1),
			new Among("\u0578\u0581", 47, 1), new Among("\u0578\u0582\u0581", 47, 1) };

	private static final char g_v[] = { 209, 4, 128, 0, 18 };

	private int I_p2;
	private int I_pV;

	private boolean r_mark_regions() {
		int v_1;
		// (, line 58
		I_pV = limit;
		I_p2 = limit;
		// do, line 62
		v_1 = cursor;
		lab0: do {
			// (, line 62
			// gopast, line 63
			golab1: while (true) {
				lab2: do {
					if (!(in_grouping(g_v, 1377, 1413))) {
						break lab2;
					}
					break golab1;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark pV, line 63
			I_pV = cursor;
			// gopast, line 63
			golab3: while (true) {
				lab4: do {
					if (!(out_grouping(g_v, 1377, 1413))) {
						break lab4;
					}
					break golab3;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 64
			golab5: while (true) {
				lab6: do {
					if (!(in_grouping(g_v, 1377, 1413))) {
						break lab6;
					}
					break golab5;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 64
			golab7: while (true) {
				lab8: do {
					if (!(out_grouping(g_v, 1377, 1413))) {
						break lab8;
					}
					break golab7;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark p2, line 64
			I_p2 = cursor;
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_adjective() {
		int among_var;
		// (, line 72
		// [, line 73
		ket = cursor;
		// substring, line 73
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			return false;
		}
		// ], line 73
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 98
			// delete, line 98
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_verb() {
		int among_var;
		// (, line 102
		// [, line 103
		ket = cursor;
		// substring, line 103
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 103
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 176
			// delete, line 176
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_noun() {
		int among_var;
		// (, line 180
		// [, line 181
		ket = cursor;
		// substring, line 181
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 181
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 223
			// delete, line 223
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_ending() {
		int among_var;
		// (, line 227
		// [, line 228
		ket = cursor;
		// substring, line 228
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 228
		bra = cursor;
		// call R2, line 228
		if (!r_R2()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 287
			// delete, line 287
			slice_del();
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		// (, line 292
		// do, line 294
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 294
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 295
		limit_backward = cursor;
		cursor = limit;
		// setlimit, line 295
		v_2 = limit - cursor;
		// tomark, line 295
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_3 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_2;
		// (, line 295
		// do, line 296
		v_4 = limit - cursor;
		lab1: do {
			// call ending, line 296
			if (!r_ending()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_4;
		// do, line 297
		v_5 = limit - cursor;
		lab2: do {
			// call verb, line 297
			if (!r_verb()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_5;
		// do, line 298
		v_6 = limit - cursor;
		lab3: do {
			// call adjective, line 298
			if (!r_adjective()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_6;
		// do, line 299
		v_7 = limit - cursor;
		lab4: do {
			// call noun, line 299
			if (!r_noun()) {
				break lab4;
			}
		} while (false);
		cursor = limit - v_7;
		limit_backward = v_3;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class basqueStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("idea", -1, 1), new Among("bidea", 0, 1), new Among("kidea", 0, 1),
			new Among("pidea", 0, 1), new Among("kundea", -1, 1), new Among("galea", -1, 1), new Among("tailea", -1, 1),
			new Among("tzailea", -1, 1), new Among("gunea", -1, 1), new Among("kunea", -1, 1),
			new Among("tzaga", -1, 1), new Among("gaia", -1, 1), new Among("aldia", -1, 1), new Among("taldia", 12, 1),
			new Among("karia", -1, 1), new Among("garria", -1, 2), new Among("karria", -1, 1), new Among("ka", -1, 1),
			new Among("tzaka", 17, 1), new Among("la", -1, 1), new Among("mena", -1, 1), new Among("pena", -1, 1),
			new Among("kina", -1, 1), new Among("ezina", -1, 1), new Among("tezina", 23, 1), new Among("kuna", -1, 1),
			new Among("tuna", -1, 1), new Among("kizuna", -1, 1), new Among("era", -1, 1), new Among("bera", 28, 1),
			new Among("arabera", 29, 4), new Among("kera", 28, 1), new Among("pera", 28, 1), new Among("orra", -1, 1),
			new Among("korra", 33, 1), new Among("dura", -1, 1), new Among("gura", -1, 1), new Among("kura", -1, 1),
			new Among("tura", -1, 1), new Among("eta", -1, 1), new Among("keta", 39, 1), new Among("gailua", -1, 1),
			new Among("eza", -1, 1), new Among("erreza", 42, 1), new Among("tza", -1, 2), new Among("gaitza", 44, 1),
			new Among("kaitza", 44, 1), new Among("kuntza", 44, 1), new Among("ide", -1, 1), new Among("bide", 48, 1),
			new Among("kide", 48, 1), new Among("pide", 48, 1), new Among("kunde", -1, 1), new Among("tzake", -1, 1),
			new Among("tzeke", -1, 1), new Among("le", -1, 1), new Among("gale", 55, 1), new Among("taile", 55, 1),
			new Among("tzaile", 55, 1), new Among("gune", -1, 1), new Among("kune", -1, 1), new Among("tze", -1, 1),
			new Among("atze", 61, 1), new Among("gai", -1, 1), new Among("aldi", -1, 1), new Among("taldi", 64, 1),
			new Among("ki", -1, 1), new Among("ari", -1, 1), new Among("kari", 67, 1), new Among("lari", 67, 1),
			new Among("tari", 67, 1), new Among("etari", 70, 1), new Among("garri", -1, 2), new Among("karri", -1, 1),
			new Among("arazi", -1, 1), new Among("tarazi", 74, 1), new Among("an", -1, 1), new Among("ean", 76, 1),
			new Among("rean", 77, 1), new Among("kan", 76, 1), new Among("etan", 76, 1), new Among("atseden", -1, 3),
			new Among("men", -1, 1), new Among("pen", -1, 1), new Among("kin", -1, 1), new Among("rekin", 84, 1),
			new Among("ezin", -1, 1), new Among("tezin", 86, 1), new Among("tun", -1, 1), new Among("kizun", -1, 1),
			new Among("go", -1, 1), new Among("ago", 90, 1), new Among("tio", -1, 1), new Among("dako", -1, 1),
			new Among("or", -1, 1), new Among("kor", 94, 1), new Among("tzat", -1, 1), new Among("du", -1, 1),
			new Among("gailu", -1, 1), new Among("tu", -1, 1), new Among("atu", 99, 1), new Among("aldatu", 100, 1),
			new Among("tatu", 100, 1), new Among("baditu", 99, 5), new Among("ez", -1, 1), new Among("errez", 104, 1),
			new Among("tzez", 104, 1), new Among("gaitz", -1, 1), new Among("kaitz", -1, 1) };

	private final static Among a_1[] = { new Among("ada", -1, 1), new Among("kada", 0, 1), new Among("anda", -1, 1),
			new Among("denda", -1, 1), new Among("gabea", -1, 1), new Among("kabea", -1, 1), new Among("aldea", -1, 1),
			new Among("kaldea", 6, 1), new Among("taldea", 6, 1), new Among("ordea", -1, 1), new Among("zalea", -1, 1),
			new Among("tzalea", 10, 1), new Among("gilea", -1, 1), new Among("emea", -1, 1), new Among("kumea", -1, 1),
			new Among("nea", -1, 1), new Among("enea", 15, 1), new Among("zionea", 15, 1), new Among("unea", 15, 1),
			new Among("gunea", 18, 1), new Among("pea", -1, 1), new Among("aurrea", -1, 1), new Among("tea", -1, 1),
			new Among("kotea", 22, 1), new Among("artea", 22, 1), new Among("ostea", 22, 1), new Among("etxea", -1, 1),
			new Among("ga", -1, 1), new Among("anga", 27, 1), new Among("gaia", -1, 1), new Among("aldia", -1, 1),
			new Among("taldia", 30, 1), new Among("handia", -1, 1), new Among("mendia", -1, 1),
			new Among("geia", -1, 1), new Among("egia", -1, 1), new Among("degia", 35, 1), new Among("tegia", 35, 1),
			new Among("nahia", -1, 1), new Among("ohia", -1, 1), new Among("kia", -1, 1), new Among("tokia", 40, 1),
			new Among("oia", -1, 1), new Among("koia", 42, 1), new Among("aria", -1, 1), new Among("karia", 44, 1),
			new Among("laria", 44, 1), new Among("taria", 44, 1), new Among("eria", -1, 1), new Among("keria", 48, 1),
			new Among("teria", 48, 1), new Among("garria", -1, 2), new Among("larria", -1, 1),
			new Among("kirria", -1, 1), new Among("duria", -1, 1), new Among("asia", -1, 1), new Among("tia", -1, 1),
			new Among("ezia", -1, 1), new Among("bizia", -1, 1), new Among("ontzia", -1, 1), new Among("ka", -1, 1),
			new Among("joka", 60, 3), new Among("aurka", 60, 10), new Among("ska", 60, 1), new Among("xka", 60, 1),
			new Among("zka", 60, 1), new Among("gibela", -1, 1), new Among("gela", -1, 1), new Among("kaila", -1, 1),
			new Among("skila", -1, 1), new Among("tila", -1, 1), new Among("ola", -1, 1), new Among("na", -1, 1),
			new Among("kana", 72, 1), new Among("ena", 72, 1), new Among("garrena", 74, 1), new Among("gerrena", 74, 1),
			new Among("urrena", 74, 1), new Among("zaina", 72, 1), new Among("tzaina", 78, 1), new Among("kina", 72, 1),
			new Among("mina", 72, 1), new Among("garna", 72, 1), new Among("una", 72, 1), new Among("duna", 83, 1),
			new Among("asuna", 83, 1), new Among("tasuna", 85, 1), new Among("ondoa", -1, 1),
			new Among("kondoa", 87, 1), new Among("ngoa", -1, 1), new Among("zioa", -1, 1), new Among("koa", -1, 1),
			new Among("takoa", 91, 1), new Among("zkoa", 91, 1), new Among("noa", -1, 1), new Among("zinoa", 94, 1),
			new Among("aroa", -1, 1), new Among("taroa", 96, 1), new Among("zaroa", 96, 1), new Among("eroa", -1, 1),
			new Among("oroa", -1, 1), new Among("osoa", -1, 1), new Among("toa", -1, 1), new Among("ttoa", 102, 1),
			new Among("ztoa", 102, 1), new Among("txoa", -1, 1), new Among("tzoa", -1, 1), new Among("\u00F1oa", -1, 1),
			new Among("ra", -1, 1), new Among("ara", 108, 1), new Among("dara", 109, 1), new Among("liara", 109, 1),
			new Among("tiara", 109, 1), new Among("tara", 109, 1), new Among("etara", 113, 1),
			new Among("tzara", 109, 1), new Among("bera", 108, 1), new Among("kera", 108, 1), new Among("pera", 108, 1),
			new Among("ora", 108, 2), new Among("tzarra", 108, 1), new Among("korra", 108, 1), new Among("tra", 108, 1),
			new Among("sa", -1, 1), new Among("osa", 123, 1), new Among("ta", -1, 1), new Among("eta", 125, 1),
			new Among("keta", 126, 1), new Among("sta", 125, 1), new Among("dua", -1, 1), new Among("mendua", 129, 1),
			new Among("ordua", 129, 1), new Among("lekua", -1, 1), new Among("burua", -1, 1), new Among("durua", -1, 1),
			new Among("tsua", -1, 1), new Among("tua", -1, 1), new Among("mentua", 136, 1), new Among("estua", 136, 1),
			new Among("txua", -1, 1), new Among("zua", -1, 1), new Among("tzua", 140, 1), new Among("za", -1, 1),
			new Among("eza", 142, 1), new Among("eroza", 142, 1), new Among("tza", 142, 2), new Among("koitza", 145, 1),
			new Among("antza", 145, 1), new Among("gintza", 145, 1), new Among("kintza", 145, 1),
			new Among("kuntza", 145, 1), new Among("gabe", -1, 1), new Among("kabe", -1, 1), new Among("kide", -1, 1),
			new Among("alde", -1, 1), new Among("kalde", 154, 1), new Among("talde", 154, 1), new Among("orde", -1, 1),
			new Among("ge", -1, 1), new Among("zale", -1, 1), new Among("tzale", 159, 1), new Among("gile", -1, 1),
			new Among("eme", -1, 1), new Among("kume", -1, 1), new Among("ne", -1, 1), new Among("zione", 164, 1),
			new Among("une", 164, 1), new Among("gune", 166, 1), new Among("pe", -1, 1), new Among("aurre", -1, 1),
			new Among("te", -1, 1), new Among("kote", 170, 1), new Among("arte", 170, 1), new Among("oste", 170, 1),
			new Among("etxe", -1, 1), new Among("gai", -1, 1), new Among("di", -1, 1), new Among("aldi", 176, 1),
			new Among("taldi", 177, 1), new Among("geldi", 176, 8), new Among("handi", 176, 1),
			new Among("mendi", 176, 1), new Among("gei", -1, 1), new Among("egi", -1, 1), new Among("degi", 183, 1),
			new Among("tegi", 183, 1), new Among("nahi", -1, 1), new Among("ohi", -1, 1), new Among("ki", -1, 1),
			new Among("toki", 188, 1), new Among("oi", -1, 1), new Among("goi", 190, 1), new Among("koi", 190, 1),
			new Among("ari", -1, 1), new Among("kari", 193, 1), new Among("lari", 193, 1), new Among("tari", 193, 1),
			new Among("garri", -1, 2), new Among("larri", -1, 1), new Among("kirri", -1, 1), new Among("duri", -1, 1),
			new Among("asi", -1, 1), new Among("ti", -1, 1), new Among("ontzi", -1, 1), new Among("\u00F1i", -1, 1),
			new Among("ak", -1, 1), new Among("ek", -1, 1), new Among("tarik", -1, 1), new Among("gibel", -1, 1),
			new Among("ail", -1, 1), new Among("kail", 209, 1), new Among("kan", -1, 1), new Among("tan", -1, 1),
			new Among("etan", 212, 1), new Among("en", -1, 4), new Among("ren", 214, 2), new Among("garren", 215, 1),
			new Among("gerren", 215, 1), new Among("urren", 215, 1), new Among("ten", 214, 4),
			new Among("tzen", 214, 4), new Among("zain", -1, 1), new Among("tzain", 221, 1), new Among("kin", -1, 1),
			new Among("min", -1, 1), new Among("dun", -1, 1), new Among("asun", -1, 1), new Among("tasun", 226, 1),
			new Among("aizun", -1, 1), new Among("ondo", -1, 1), new Among("kondo", 229, 1), new Among("go", -1, 1),
			new Among("ngo", 231, 1), new Among("zio", -1, 1), new Among("ko", -1, 1), new Among("trako", 234, 5),
			new Among("tako", 234, 1), new Among("etako", 236, 1), new Among("eko", 234, 1),
			new Among("tariko", 234, 1), new Among("sko", 234, 1), new Among("tuko", 234, 1),
			new Among("minutuko", 241, 6), new Among("zko", 234, 1), new Among("no", -1, 1), new Among("zino", 244, 1),
			new Among("ro", -1, 1), new Among("aro", 246, 1), new Among("igaro", 247, 9), new Among("taro", 247, 1),
			new Among("zaro", 247, 1), new Among("ero", 246, 1), new Among("giro", 246, 1), new Among("oro", 246, 1),
			new Among("oso", -1, 1), new Among("to", -1, 1), new Among("tto", 255, 1), new Among("zto", 255, 1),
			new Among("txo", -1, 1), new Among("tzo", -1, 1), new Among("gintzo", 259, 1), new Among("\u00F1o", -1, 1),
			new Among("zp", -1, 1), new Among("ar", -1, 1), new Among("dar", 263, 1), new Among("behar", 263, 1),
			new Among("zehar", 263, 7), new Among("liar", 263, 1), new Among("tiar", 263, 1), new Among("tar", 263, 1),
			new Among("tzar", 263, 1), new Among("or", -1, 2), new Among("kor", 271, 1), new Among("os", -1, 1),
			new Among("ket", -1, 1), new Among("du", -1, 1), new Among("mendu", 275, 1), new Among("ordu", 275, 1),
			new Among("leku", -1, 1), new Among("buru", -1, 2), new Among("duru", -1, 1), new Among("tsu", -1, 1),
			new Among("tu", -1, 1), new Among("tatu", 282, 4), new Among("mentu", 282, 1), new Among("estu", 282, 1),
			new Among("txu", -1, 1), new Among("zu", -1, 1), new Among("tzu", 287, 1), new Among("gintzu", 288, 1),
			new Among("z", -1, 1), new Among("ez", 290, 1), new Among("eroz", 290, 1), new Among("tz", 290, 1),
			new Among("koitz", 293, 1) };

	private final static Among a_2[] = { new Among("zlea", -1, 2), new Among("keria", -1, 1), new Among("la", -1, 1),
			new Among("era", -1, 1), new Among("dade", -1, 1), new Among("tade", -1, 1), new Among("date", -1, 1),
			new Among("tate", -1, 1), new Among("gi", -1, 1), new Among("ki", -1, 1), new Among("ik", -1, 1),
			new Among("lanik", 10, 1), new Among("rik", 10, 1), new Among("larik", 12, 1), new Among("ztik", 10, 1),
			new Among("go", -1, 1), new Among("ro", -1, 1), new Among("ero", 16, 1), new Among("to", -1, 1) };

	private static final char g_v[] = { 17, 65, 16 };

	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_3;
		int v_6;
		int v_8;
		// (, line 23
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 29
		v_1 = cursor;
		lab0: do {
			// (, line 29
			// or, line 31
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// (, line 30
					if (!(in_grouping(g_v, 97, 117))) {
						break lab2;
					}
					// or, line 30
					lab3: do {
						v_3 = cursor;
						lab4: do {
							// (, line 30
							if (!(out_grouping(g_v, 97, 117))) {
								break lab4;
							}
							// gopast, line 30
							golab5: while (true) {
								lab6: do {
									if (!(in_grouping(g_v, 97, 117))) {
										break lab6;
									}
									break golab5;
								} while (false);
								if (cursor >= limit) {
									break lab4;
								}
								cursor++;
							}
							break lab3;
						} while (false);
						cursor = v_3;
						// (, line 30
						if (!(in_grouping(g_v, 97, 117))) {
							break lab2;
						}
						// gopast, line 30
						golab7: while (true) {
							lab8: do {
								if (!(out_grouping(g_v, 97, 117))) {
									break lab8;
								}
								break golab7;
							} while (false);
							if (cursor >= limit) {
								break lab2;
							}
							cursor++;
						}
					} while (false);
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 32
				if (!(out_grouping(g_v, 97, 117))) {
					break lab0;
				}
				// or, line 32
				lab9: do {
					v_6 = cursor;
					lab10: do {
						// (, line 32
						if (!(out_grouping(g_v, 97, 117))) {
							break lab10;
						}
						// gopast, line 32
						golab11: while (true) {
							lab12: do {
								if (!(in_grouping(g_v, 97, 117))) {
									break lab12;
								}
								break golab11;
							} while (false);
							if (cursor >= limit) {
								break lab10;
							}
							cursor++;
						}
						break lab9;
					} while (false);
					cursor = v_6;
					// (, line 32
					if (!(in_grouping(g_v, 97, 117))) {
						break lab0;
					}
					// next, line 32
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				} while (false);
			} while (false);
			// setmark pV, line 33
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 35
		v_8 = cursor;
		lab13: do {
			// (, line 35
			// gopast, line 36
			golab14: while (true) {
				lab15: do {
					if (!(in_grouping(g_v, 97, 117))) {
						break lab15;
					}
					break golab14;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 36
			golab16: while (true) {
				lab17: do {
					if (!(out_grouping(g_v, 97, 117))) {
						break lab17;
					}
					break golab16;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p1, line 36
			I_p1 = cursor;
			// gopast, line 37
			golab18: while (true) {
				lab19: do {
					if (!(in_grouping(g_v, 97, 117))) {
						break lab19;
					}
					break golab18;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 37
			golab20: while (true) {
				lab21: do {
					if (!(out_grouping(g_v, 97, 117))) {
						break lab21;
					}
					break golab20;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p2, line 37
			I_p2 = cursor;
		} while (false);
		cursor = v_8;
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_aditzak() {
		int among_var;
		// (, line 47
		// [, line 48
		ket = cursor;
		// substring, line 48
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			return false;
		}
		// ], line 48
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 59
			// call RV, line 59
			if (!r_RV()) {
				return false;
			}
			// delete, line 59
			slice_del();
			break;
		case 2:
			// (, line 61
			// call R2, line 61
			if (!r_R2()) {
				return false;
			}
			// delete, line 61
			slice_del();
			break;
		case 3:
			// (, line 63
			// <-, line 63
			slice_from("atseden");
			break;
		case 4:
			// (, line 65
			// <-, line 65
			slice_from("arabera");
			break;
		case 5:
			// (, line 67
			// <-, line 67
			slice_from("baditu");
			break;
		}
		return true;
	}

	private boolean r_izenak() {
		int among_var;
		// (, line 72
		// [, line 73
		ket = cursor;
		// substring, line 73
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 73
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 103
			// call RV, line 103
			if (!r_RV()) {
				return false;
			}
			// delete, line 103
			slice_del();
			break;
		case 2:
			// (, line 105
			// call R2, line 105
			if (!r_R2()) {
				return false;
			}
			// delete, line 105
			slice_del();
			break;
		case 3:
			// (, line 107
			// <-, line 107
			slice_from("jok");
			break;
		case 4:
			// (, line 109
			// call R1, line 109
			if (!r_R1()) {
				return false;
			}
			// delete, line 109
			slice_del();
			break;
		case 5:
			// (, line 111
			// <-, line 111
			slice_from("tra");
			break;
		case 6:
			// (, line 113
			// <-, line 113
			slice_from("minutu");
			break;
		case 7:
			// (, line 115
			// <-, line 115
			slice_from("zehar");
			break;
		case 8:
			// (, line 117
			// <-, line 117
			slice_from("geldi");
			break;
		case 9:
			// (, line 119
			// <-, line 119
			slice_from("igaro");
			break;
		case 10:
			// (, line 121
			// <-, line 121
			slice_from("aurka");
			break;
		}
		return true;
	}

	private boolean r_adjetiboak() {
		int among_var;
		// (, line 125
		// [, line 126
		ket = cursor;
		// substring, line 126
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 126
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 129
			// call RV, line 129
			if (!r_RV()) {
				return false;
			}
			// delete, line 129
			slice_del();
			break;
		case 2:
			// (, line 131
			// <-, line 131
			slice_from("z");
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 137
		// do, line 138
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 138
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 139
		limit_backward = cursor;
		cursor = limit;
		// (, line 139
		// repeat, line 140
		replab1: while (true) {
			v_2 = limit - cursor;
			lab2: do {
				// call aditzak, line 140
				if (!r_aditzak()) {
					break lab2;
				}
				continue replab1;
			} while (false);
			cursor = limit - v_2;
			break replab1;
		}
		// repeat, line 141
		replab3: while (true) {
			v_3 = limit - cursor;
			lab4: do {
				// call izenak, line 141
				if (!r_izenak()) {
					break lab4;
				}
				continue replab3;
			} while (false);
			cursor = limit - v_3;
			break replab3;
		}
		// do, line 142
		v_4 = limit - cursor;
		lab5: do {
			// call adjetiboak, line 142
			if (!r_adjetiboak()) {
				break lab5;
			}
		} while (false);
		cursor = limit - v_4;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class catalanStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 13), new Among("\u00B7", 0, 12), new Among("\u00E0", 0, 2),
			new Among("\u00E1", 0, 1), new Among("\u00E8", 0, 4), new Among("\u00E9", 0, 3), new Among("\u00EC", 0, 6),
			new Among("\u00ED", 0, 5), new Among("\u00EF", 0, 11), new Among("\u00F2", 0, 8), new Among("\u00F3", 0, 7),
			new Among("\u00FA", 0, 9), new Among("\u00FC", 0, 10) };

	private final static Among a_1[] = { new Among("la", -1, 1), new Among("-la", 0, 1), new Among("sela", 0, 1),
			new Among("le", -1, 1), new Among("me", -1, 1), new Among("-me", 4, 1), new Among("se", -1, 1),
			new Among("-te", -1, 1), new Among("hi", -1, 1), new Among("'hi", 8, 1), new Among("li", -1, 1),
			new Among("-li", 10, 1), new Among("'l", -1, 1), new Among("'m", -1, 1), new Among("-m", -1, 1),
			new Among("'n", -1, 1), new Among("-n", -1, 1), new Among("ho", -1, 1), new Among("'ho", 17, 1),
			new Among("lo", -1, 1), new Among("selo", 19, 1), new Among("'s", -1, 1), new Among("las", -1, 1),
			new Among("selas", 22, 1), new Among("les", -1, 1), new Among("-les", 24, 1), new Among("'ls", -1, 1),
			new Among("-ls", -1, 1), new Among("'ns", -1, 1), new Among("-ns", -1, 1), new Among("ens", -1, 1),
			new Among("los", -1, 1), new Among("selos", 31, 1), new Among("nos", -1, 1), new Among("-nos", 33, 1),
			new Among("vos", -1, 1), new Among("us", -1, 1), new Among("-us", 36, 1), new Among("'t", -1, 1) };

	private final static Among a_2[] = { new Among("ica", -1, 4), new Among("l\u00F3gica", 0, 3),
			new Among("enca", -1, 1), new Among("ada", -1, 2), new Among("ancia", -1, 1), new Among("encia", -1, 1),
			new Among("\u00E8ncia", -1, 1), new Among("\u00EDcia", -1, 1), new Among("logia", -1, 3),
			new Among("inia", -1, 1), new Among("\u00EDinia", 9, 1), new Among("eria", -1, 1),
			new Among("\u00E0ria", -1, 1), new Among("at\u00F2ria", -1, 1), new Among("alla", -1, 1),
			new Among("ella", -1, 1), new Among("\u00EDvola", -1, 1), new Among("ima", -1, 1),
			new Among("\u00EDssima", 17, 1), new Among("qu\u00EDssima", 18, 5), new Among("ana", -1, 1),
			new Among("ina", -1, 1), new Among("era", -1, 1), new Among("sfera", 22, 1), new Among("ora", -1, 1),
			new Among("dora", 24, 1), new Among("adora", 25, 1), new Among("adura", -1, 1), new Among("esa", -1, 1),
			new Among("osa", -1, 1), new Among("assa", -1, 1), new Among("essa", -1, 1), new Among("issa", -1, 1),
			new Among("eta", -1, 1), new Among("ita", -1, 1), new Among("ota", -1, 1), new Among("ista", -1, 1),
			new Among("ialista", 36, 1), new Among("ionista", 36, 1), new Among("iva", -1, 1),
			new Among("ativa", 39, 1), new Among("n\u00E7a", -1, 1), new Among("log\u00EDa", -1, 3),
			new Among("ic", -1, 4), new Among("\u00EDstic", 43, 1), new Among("enc", -1, 1), new Among("esc", -1, 1),
			new Among("ud", -1, 1), new Among("atge", -1, 1), new Among("ble", -1, 1), new Among("able", 49, 1),
			new Among("ible", 49, 1), new Among("isme", -1, 1), new Among("ialisme", 52, 1),
			new Among("ionisme", 52, 1), new Among("ivisme", 52, 1), new Among("aire", -1, 1), new Among("icte", -1, 1),
			new Among("iste", -1, 1), new Among("ici", -1, 1), new Among("\u00EDci", -1, 1), new Among("logi", -1, 3),
			new Among("ari", -1, 1), new Among("tori", -1, 1), new Among("al", -1, 1), new Among("il", -1, 1),
			new Among("all", -1, 1), new Among("ell", -1, 1), new Among("\u00EDvol", -1, 1), new Among("isam", -1, 1),
			new Among("issem", -1, 1), new Among("\u00ECssem", -1, 1), new Among("\u00EDssem", -1, 1),
			new Among("\u00EDssim", -1, 1), new Among("qu\u00EDssim", 73, 5), new Among("amen", -1, 1),
			new Among("\u00ECssin", -1, 1), new Among("ar", -1, 1), new Among("ificar", 77, 1),
			new Among("egar", 77, 1), new Among("ejar", 77, 1), new Among("itar", 77, 1), new Among("itzar", 77, 1),
			new Among("fer", -1, 1), new Among("or", -1, 1), new Among("dor", 84, 1), new Among("dur", -1, 1),
			new Among("doras", -1, 1), new Among("ics", -1, 4), new Among("l\u00F3gics", 88, 3),
			new Among("uds", -1, 1), new Among("nces", -1, 1), new Among("ades", -1, 2), new Among("ancies", -1, 1),
			new Among("encies", -1, 1), new Among("\u00E8ncies", -1, 1), new Among("\u00EDcies", -1, 1),
			new Among("logies", -1, 3), new Among("inies", -1, 1), new Among("\u00EDnies", -1, 1),
			new Among("eries", -1, 1), new Among("\u00E0ries", -1, 1), new Among("at\u00F2ries", -1, 1),
			new Among("bles", -1, 1), new Among("ables", 103, 1), new Among("ibles", 103, 1), new Among("imes", -1, 1),
			new Among("\u00EDssimes", 106, 1), new Among("qu\u00EDssimes", 107, 5), new Among("formes", -1, 1),
			new Among("ismes", -1, 1), new Among("ialismes", 110, 1), new Among("ines", -1, 1),
			new Among("eres", -1, 1), new Among("ores", -1, 1), new Among("dores", 114, 1), new Among("idores", 115, 1),
			new Among("dures", -1, 1), new Among("eses", -1, 1), new Among("oses", -1, 1), new Among("asses", -1, 1),
			new Among("ictes", -1, 1), new Among("ites", -1, 1), new Among("otes", -1, 1), new Among("istes", -1, 1),
			new Among("ialistes", 124, 1), new Among("ionistes", 124, 1), new Among("iques", -1, 4),
			new Among("l\u00F3giques", 127, 3), new Among("ives", -1, 1), new Among("atives", 129, 1),
			new Among("log\u00EDes", -1, 3), new Among("alleng\u00FCes", -1, 1), new Among("icis", -1, 1),
			new Among("\u00EDcis", -1, 1), new Among("logis", -1, 3), new Among("aris", -1, 1),
			new Among("toris", -1, 1), new Among("ls", -1, 1), new Among("als", 138, 1), new Among("ells", 138, 1),
			new Among("ims", -1, 1), new Among("\u00EDssims", 141, 1), new Among("qu\u00EDssims", 142, 5),
			new Among("ions", -1, 1), new Among("cions", 144, 1), new Among("acions", 145, 2), new Among("esos", -1, 1),
			new Among("osos", -1, 1), new Among("assos", -1, 1), new Among("issos", -1, 1), new Among("ers", -1, 1),
			new Among("ors", -1, 1), new Among("dors", 152, 1), new Among("adors", 153, 1), new Among("idors", 153, 1),
			new Among("ats", -1, 1), new Among("itats", 156, 1), new Among("bilitats", 157, 1),
			new Among("ivitats", 157, 1), new Among("ativitats", 159, 1), new Among("\u00EFtats", 156, 1),
			new Among("ets", -1, 1), new Among("ants", -1, 1), new Among("ents", -1, 1), new Among("ments", 164, 1),
			new Among("aments", 165, 1), new Among("ots", -1, 1), new Among("uts", -1, 1), new Among("ius", -1, 1),
			new Among("trius", 169, 1), new Among("atius", 169, 1), new Among("\u00E8s", -1, 1),
			new Among("\u00E9s", -1, 1), new Among("\u00EDs", -1, 1), new Among("d\u00EDs", 174, 1),
			new Among("\u00F3s", -1, 1), new Among("itat", -1, 1), new Among("bilitat", 177, 1),
			new Among("ivitat", 177, 1), new Among("ativitat", 179, 1), new Among("\u00EFtat", -1, 1),
			new Among("et", -1, 1), new Among("ant", -1, 1), new Among("ent", -1, 1), new Among("ient", 184, 1),
			new Among("ment", 184, 1), new Among("ament", 186, 1), new Among("isament", 187, 1), new Among("ot", -1, 1),
			new Among("isseu", -1, 1), new Among("\u00ECsseu", -1, 1), new Among("\u00EDsseu", -1, 1),
			new Among("triu", -1, 1), new Among("\u00EDssiu", -1, 1), new Among("atiu", -1, 1),
			new Among("\u00F3", -1, 1), new Among("i\u00F3", 196, 1), new Among("ci\u00F3", 197, 1),
			new Among("aci\u00F3", 198, 1) };

	private final static Among a_3[] = { new Among("aba", -1, 1), new Among("esca", -1, 1), new Among("isca", -1, 1),
			new Among("\u00EFsca", -1, 1), new Among("ada", -1, 1), new Among("ida", -1, 1), new Among("uda", -1, 1),
			new Among("\u00EFda", -1, 1), new Among("ia", -1, 1), new Among("aria", 8, 1), new Among("iria", 8, 1),
			new Among("ara", -1, 1), new Among("iera", -1, 1), new Among("ira", -1, 1), new Among("adora", -1, 1),
			new Among("\u00EFra", -1, 1), new Among("ava", -1, 1), new Among("ixa", -1, 1), new Among("itza", -1, 1),
			new Among("\u00EDa", -1, 1), new Among("ar\u00EDa", 19, 1), new Among("er\u00EDa", 19, 1),
			new Among("ir\u00EDa", 19, 1), new Among("\u00EFa", -1, 1), new Among("isc", -1, 1),
			new Among("\u00EFsc", -1, 1), new Among("ad", -1, 1), new Among("ed", -1, 1), new Among("id", -1, 1),
			new Among("ie", -1, 1), new Among("re", -1, 1), new Among("dre", 30, 1), new Among("ase", -1, 1),
			new Among("iese", -1, 1), new Among("aste", -1, 1), new Among("iste", -1, 1), new Among("ii", -1, 1),
			new Among("ini", -1, 1), new Among("esqui", -1, 1), new Among("eixi", -1, 1), new Among("itzi", -1, 1),
			new Among("am", -1, 1), new Among("em", -1, 1), new Among("arem", 42, 1), new Among("irem", 42, 1),
			new Among("\u00E0rem", 42, 1), new Among("\u00EDrem", 42, 1), new Among("\u00E0ssem", 42, 1),
			new Among("\u00E9ssem", 42, 1), new Among("iguem", 42, 1), new Among("\u00EFguem", 42, 1),
			new Among("avem", 42, 1), new Among("\u00E0vem", 42, 1), new Among("\u00E1vem", 42, 1),
			new Among("ir\u00ECem", 42, 1), new Among("\u00EDem", 42, 1), new Among("ar\u00EDem", 55, 1),
			new Among("ir\u00EDem", 55, 1), new Among("assim", -1, 1), new Among("essim", -1, 1),
			new Among("issim", -1, 1), new Among("\u00E0ssim", -1, 1), new Among("\u00E8ssim", -1, 1),
			new Among("\u00E9ssim", -1, 1), new Among("\u00EDssim", -1, 1), new Among("\u00EFm", -1, 1),
			new Among("an", -1, 1), new Among("aban", 66, 1), new Among("arian", 66, 1), new Among("aran", 66, 1),
			new Among("ieran", 66, 1), new Among("iran", 66, 1), new Among("\u00EDan", 66, 1),
			new Among("ar\u00EDan", 72, 1), new Among("er\u00EDan", 72, 1), new Among("ir\u00EDan", 72, 1),
			new Among("en", -1, 1), new Among("ien", 76, 1), new Among("arien", 77, 1), new Among("irien", 77, 1),
			new Among("aren", 76, 1), new Among("eren", 76, 1), new Among("iren", 76, 1), new Among("\u00E0ren", 76, 1),
			new Among("\u00EFren", 76, 1), new Among("asen", 76, 1), new Among("iesen", 76, 1),
			new Among("assen", 76, 1), new Among("essen", 76, 1), new Among("issen", 76, 1),
			new Among("\u00E9ssen", 76, 1), new Among("\u00EFssen", 76, 1), new Among("esquen", 76, 1),
			new Among("isquen", 76, 1), new Among("\u00EFsquen", 76, 1), new Among("aven", 76, 1),
			new Among("ixen", 76, 1), new Among("eixen", 96, 1), new Among("\u00EFxen", 76, 1),
			new Among("\u00EFen", 76, 1), new Among("in", -1, 1), new Among("inin", 100, 1), new Among("sin", 100, 1),
			new Among("isin", 102, 1), new Among("assin", 102, 1), new Among("essin", 102, 1),
			new Among("issin", 102, 1), new Among("\u00EFssin", 102, 1), new Among("esquin", 100, 1),
			new Among("eixin", 100, 1), new Among("aron", -1, 1), new Among("ieron", -1, 1),
			new Among("ar\u00E1n", -1, 1), new Among("er\u00E1n", -1, 1), new Among("ir\u00E1n", -1, 1),
			new Among("i\u00EFn", -1, 1), new Among("ado", -1, 1), new Among("ido", -1, 1), new Among("ando", -1, 2),
			new Among("iendo", -1, 1), new Among("io", -1, 1), new Among("ixo", -1, 1), new Among("eixo", 121, 1),
			new Among("\u00EFxo", -1, 1), new Among("itzo", -1, 1), new Among("ar", -1, 1), new Among("tzar", 125, 1),
			new Among("er", -1, 1), new Among("eixer", 127, 1), new Among("ir", -1, 1), new Among("ador", -1, 1),
			new Among("as", -1, 1), new Among("abas", 131, 1), new Among("adas", 131, 1), new Among("idas", 131, 1),
			new Among("aras", 131, 1), new Among("ieras", 131, 1), new Among("\u00EDas", 131, 1),
			new Among("ar\u00EDas", 137, 1), new Among("er\u00EDas", 137, 1), new Among("ir\u00EDas", 137, 1),
			new Among("ids", -1, 1), new Among("es", -1, 1), new Among("ades", 142, 1), new Among("ides", 142, 1),
			new Among("udes", 142, 1), new Among("\u00EFdes", 142, 1), new Among("atges", 142, 1),
			new Among("ies", 142, 1), new Among("aries", 148, 1), new Among("iries", 148, 1), new Among("ares", 142, 1),
			new Among("ires", 142, 1), new Among("adores", 142, 1), new Among("\u00EFres", 142, 1),
			new Among("ases", 142, 1), new Among("ieses", 142, 1), new Among("asses", 142, 1),
			new Among("esses", 142, 1), new Among("isses", 142, 1), new Among("\u00EFsses", 142, 1),
			new Among("ques", 142, 1), new Among("esques", 161, 1), new Among("\u00EFsques", 161, 1),
			new Among("aves", 142, 1), new Among("ixes", 142, 1), new Among("eixes", 165, 1),
			new Among("\u00EFxes", 142, 1), new Among("\u00EFes", 142, 1), new Among("abais", -1, 1),
			new Among("arais", -1, 1), new Among("ierais", -1, 1), new Among("\u00EDais", -1, 1),
			new Among("ar\u00EDais", 172, 1), new Among("er\u00EDais", 172, 1), new Among("ir\u00EDais", 172, 1),
			new Among("aseis", -1, 1), new Among("ieseis", -1, 1), new Among("asteis", -1, 1),
			new Among("isteis", -1, 1), new Among("inis", -1, 1), new Among("sis", -1, 1), new Among("isis", 181, 1),
			new Among("assis", 181, 1), new Among("essis", 181, 1), new Among("issis", 181, 1),
			new Among("\u00EFssis", 181, 1), new Among("esquis", -1, 1), new Among("eixis", -1, 1),
			new Among("itzis", -1, 1), new Among("\u00E1is", -1, 1), new Among("ar\u00E9is", -1, 1),
			new Among("er\u00E9is", -1, 1), new Among("ir\u00E9is", -1, 1), new Among("ams", -1, 1),
			new Among("ados", -1, 1), new Among("idos", -1, 1), new Among("amos", -1, 1),
			new Among("\u00E1bamos", 197, 1), new Among("\u00E1ramos", 197, 1), new Among("i\u00E9ramos", 197, 1),
			new Among("\u00EDamos", 197, 1), new Among("ar\u00EDamos", 201, 1), new Among("er\u00EDamos", 201, 1),
			new Among("ir\u00EDamos", 201, 1), new Among("aremos", -1, 1), new Among("eremos", -1, 1),
			new Among("iremos", -1, 1), new Among("\u00E1semos", -1, 1), new Among("i\u00E9semos", -1, 1),
			new Among("imos", -1, 1), new Among("adors", -1, 1), new Among("ass", -1, 1), new Among("erass", 212, 1),
			new Among("ess", -1, 1), new Among("ats", -1, 1), new Among("its", -1, 1), new Among("ents", -1, 1),
			new Among("\u00E0s", -1, 1), new Among("ar\u00E0s", 218, 1), new Among("ir\u00E0s", 218, 1),
			new Among("ar\u00E1s", -1, 1), new Among("er\u00E1s", -1, 1), new Among("ir\u00E1s", -1, 1),
			new Among("\u00E9s", -1, 1), new Among("ar\u00E9s", 224, 1), new Among("\u00EDs", -1, 1),
			new Among("i\u00EFs", -1, 1), new Among("at", -1, 1), new Among("it", -1, 1), new Among("ant", -1, 1),
			new Among("ent", -1, 1), new Among("int", -1, 1), new Among("ut", -1, 1), new Among("\u00EFt", -1, 1),
			new Among("au", -1, 1), new Among("erau", 235, 1), new Among("ieu", -1, 1), new Among("ineu", -1, 1),
			new Among("areu", -1, 1), new Among("ireu", -1, 1), new Among("\u00E0reu", -1, 1),
			new Among("\u00EDreu", -1, 1), new Among("asseu", -1, 1), new Among("esseu", -1, 1),
			new Among("eresseu", 244, 1), new Among("\u00E0sseu", -1, 1), new Among("\u00E9sseu", -1, 1),
			new Among("igueu", -1, 1), new Among("\u00EFgueu", -1, 1), new Among("\u00E0veu", -1, 1),
			new Among("\u00E1veu", -1, 1), new Among("itzeu", -1, 1), new Among("\u00ECeu", -1, 1),
			new Among("ir\u00ECeu", 253, 1), new Among("\u00EDeu", -1, 1), new Among("ar\u00EDeu", 255, 1),
			new Among("ir\u00EDeu", 255, 1), new Among("assiu", -1, 1), new Among("issiu", -1, 1),
			new Among("\u00E0ssiu", -1, 1), new Among("\u00E8ssiu", -1, 1), new Among("\u00E9ssiu", -1, 1),
			new Among("\u00EDssiu", -1, 1), new Among("\u00EFu", -1, 1), new Among("ix", -1, 1),
			new Among("eix", 265, 1), new Among("\u00EFx", -1, 1), new Among("itz", -1, 1), new Among("i\u00E0", -1, 1),
			new Among("ar\u00E0", -1, 1), new Among("ir\u00E0", -1, 1), new Among("itz\u00E0", -1, 1),
			new Among("ar\u00E1", -1, 1), new Among("er\u00E1", -1, 1), new Among("ir\u00E1", -1, 1),
			new Among("ir\u00E8", -1, 1), new Among("ar\u00E9", -1, 1), new Among("er\u00E9", -1, 1),
			new Among("ir\u00E9", -1, 1), new Among("\u00ED", -1, 1), new Among("i\u00EF", -1, 1),
			new Among("i\u00F3", -1, 1) };

	private final static Among a_4[] = { new Among("a", -1, 1), new Among("e", -1, 1), new Among("i", -1, 1),
			new Among("\u00EFn", -1, 1), new Among("o", -1, 1), new Among("ir", -1, 1), new Among("s", -1, 1),
			new Among("is", 6, 1), new Among("os", 6, 1), new Among("\u00EFs", 6, 1), new Among("it", -1, 1),
			new Among("eu", -1, 1), new Among("iu", -1, 1), new Among("iqu", -1, 2), new Among("itz", -1, 1),
			new Among("\u00E0", -1, 1), new Among("\u00E1", -1, 1), new Among("\u00E9", -1, 1),
			new Among("\u00EC", -1, 1), new Among("\u00ED", -1, 1), new Among("\u00EF", -1, 1),
			new Among("\u00F3", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 129, 81, 6, 10 };

	private int I_p2;
	private int I_p1;

	private boolean r_mark_regions() {
		int v_1;
		// (, line 37
		I_p1 = limit;
		I_p2 = limit;
		// do, line 42
		v_1 = cursor;
		lab0: do {
			// (, line 42
			// gopast, line 43
			golab1: while (true) {
				lab2: do {
					if (!(in_grouping(g_v, 97, 252))) {
						break lab2;
					}
					break golab1;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 43
			golab3: while (true) {
				lab4: do {
					if (!(out_grouping(g_v, 97, 252))) {
						break lab4;
					}
					break golab3;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark p1, line 43
			I_p1 = cursor;
			// gopast, line 44
			golab5: while (true) {
				lab6: do {
					if (!(in_grouping(g_v, 97, 252))) {
						break lab6;
					}
					break golab5;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 44
			golab7: while (true) {
				lab8: do {
					if (!(out_grouping(g_v, 97, 252))) {
						break lab8;
					}
					break golab7;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark p2, line 44
			I_p2 = cursor;
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_cleaning() {
		int among_var;
		int v_1;
		// repeat, line 48
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 48
				// [, line 49
				bra = cursor;
				// substring, line 49
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 49
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 50
					// <-, line 50
					slice_from("a");
					break;
				case 2:
					// (, line 51
					// <-, line 51
					slice_from("a");
					break;
				case 3:
					// (, line 52
					// <-, line 52
					slice_from("e");
					break;
				case 4:
					// (, line 53
					// <-, line 53
					slice_from("e");
					break;
				case 5:
					// (, line 54
					// <-, line 54
					slice_from("i");
					break;
				case 6:
					// (, line 55
					// <-, line 55
					slice_from("i");
					break;
				case 7:
					// (, line 56
					// <-, line 56
					slice_from("o");
					break;
				case 8:
					// (, line 57
					// <-, line 57
					slice_from("o");
					break;
				case 9:
					// (, line 58
					// <-, line 58
					slice_from("u");
					break;
				case 10:
					// (, line 59
					// <-, line 59
					slice_from("u");
					break;
				case 11:
					// (, line 60
					// <-, line 60
					slice_from("i");
					break;
				case 12:
					// (, line 61
					// <-, line 61
					slice_from(".");
					break;
				case 13:
					// (, line 62
					// next, line 62
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_attached_pronoun() {
		int among_var;
		// (, line 71
		// [, line 72
		ket = cursor;
		// substring, line 72
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 72
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 82
			// call R1, line 82
			if (!r_R1()) {
				return false;
			}
			// delete, line 82
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		// (, line 86
		// [, line 87
		ket = cursor;
		// substring, line 87
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 87
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 111
			// call R1, line 111
			if (!r_R1()) {
				return false;
			}
			// delete, line 111
			slice_del();
			break;
		case 2:
			// (, line 113
			// call R2, line 113
			if (!r_R2()) {
				return false;
			}
			// delete, line 113
			slice_del();
			break;
		case 3:
			// (, line 115
			// call R2, line 115
			if (!r_R2()) {
				return false;
			}
			// <-, line 115
			slice_from("log");
			break;
		case 4:
			// (, line 117
			// call R2, line 117
			if (!r_R2()) {
				return false;
			}
			// <-, line 117
			slice_from("ic");
			break;
		case 5:
			// (, line 119
			// call R1, line 119
			if (!r_R1()) {
				return false;
			}
			// <-, line 119
			slice_from("c");
			break;
		}
		return true;
	}

	private boolean r_verb_suffix() {
		int among_var;
		// (, line 123
		// [, line 124
		ket = cursor;
		// substring, line 124
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 124
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 169
			// call R1, line 169
			if (!r_R1()) {
				return false;
			}
			// delete, line 169
			slice_del();
			break;
		case 2:
			// (, line 171
			// call R2, line 171
			if (!r_R2()) {
				return false;
			}
			// delete, line 171
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_residual_suffix() {
		int among_var;
		// (, line 175
		// [, line 176
		ket = cursor;
		// substring, line 176
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 176
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 179
			// call R1, line 179
			if (!r_R1()) {
				return false;
			}
			// delete, line 179
			slice_del();
			break;
		case 2:
			// (, line 181
			// call R1, line 181
			if (!r_R1()) {
				return false;
			}
			// <-, line 181
			slice_from("ic");
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		// (, line 186
		// do, line 187
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 187
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 188
		limit_backward = cursor;
		cursor = limit;
		// (, line 188
		// do, line 189
		v_2 = limit - cursor;
		lab1: do {
			// call attached_pronoun, line 189
			if (!r_attached_pronoun()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 190
		v_3 = limit - cursor;
		lab2: do {
			// (, line 190
			// or, line 190
			lab3: do {
				v_4 = limit - cursor;
				lab4: do {
					// call standard_suffix, line 190
					if (!r_standard_suffix()) {
						break lab4;
					}
					break lab3;
				} while (false);
				cursor = limit - v_4;
				// call verb_suffix, line 191
				if (!r_verb_suffix()) {
					break lab2;
				}
			} while (false);
		} while (false);
		cursor = limit - v_3;
		// do, line 193
		v_5 = limit - cursor;
		lab5: do {
			// call residual_suffix, line 193
			if (!r_residual_suffix()) {
				break lab5;
			}
		} while (false);
		cursor = limit - v_5;
		cursor = limit_backward; // do, line 195
		v_6 = cursor;
		lab6: do {
			// call cleaning, line 195
			if (!r_cleaning()) {
				break lab6;
			}
		} while (false);
		cursor = v_6;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class czechStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("ce", -1, 1), new Among("ze", -1, 2), new Among("\u017Ee", -1, 2),
			new Among("ci", -1, 1), new Among("\u010Dti", -1, 3), new Among("\u0161ti", -1, 4), new Among("zi", -1, 2),
			new Among("\u010Di", -1, 1), new Among("\u017Ei", -1, 2), new Among("\u010Dt\u00E9", -1, 3),
			new Among("\u0161t\u00E9", -1, 4), new Among("\u010D", -1, 1), new Among("\u010Dt\u011B", -1, 3),
			new Among("\u0161t\u011B", -1, 4) };

	private final static Among a_1[] = { new Among("in", -1, 2), new Among("ov", -1, 1), new Among("\u016Fv", -1, 1) };

	private final static Among a_2[] = { new Among("a", -1, 1), new Among("ama", 0, 1), new Among("ata", 0, 1),
			new Among("e", -1, 2), new Among("\u011Bte", 3, 2), new Among("ech", -1, 2), new Among("atech", 5, 1),
			new Among("ich", -1, 2), new Among("\u00E1ch", -1, 1), new Among("\u00EDch", -1, 2),
			new Among("\u00FDch", -1, 1), new Among("i", -1, 2), new Among("mi", 11, 1), new Among("ami", 12, 1),
			new Among("emi", 12, 2), new Among("\u00EDmi", 12, 2), new Among("\u00FDmi", 12, 1),
			new Among("\u011Bmi", 12, 2), new Among("\u011Bti", 11, 2), new Among("ovi", 11, 1), new Among("em", -1, 3),
			new Among("\u011Btem", 20, 1), new Among("\u00E1m", -1, 1), new Among("\u00E9m", -1, 2),
			new Among("\u00EDm", -1, 2), new Among("\u00FDm", -1, 1), new Among("at\u016Fm", -1, 1),
			new Among("o", -1, 1), new Among("iho", 27, 2), new Among("\u00E9ho", 27, 2), new Among("\u00EDho", 27, 2),
			new Among("es", -1, 2), new Among("os", -1, 1), new Among("us", -1, 1), new Among("at", -1, 1),
			new Among("u", -1, 1), new Among("imu", 35, 2), new Among("\u00E9mu", 35, 2), new Among("ou", 35, 1),
			new Among("y", -1, 1), new Among("aty", 39, 1), new Among("\u00E1", -1, 1), new Among("\u00E9", -1, 1),
			new Among("ov\u00E9", 42, 1), new Among("\u00ED", -1, 2), new Among("\u00FD", -1, 1),
			new Among("\u011B", -1, 2), new Among("\u016F", -1, 1) };

	private final static Among a_3[] = { new Among("ob", -1, 1), new Among("itb", -1, 2), new Among("ec", -1, 3),
			new Among("inec", 2, 2), new Among("obinec", 3, 1), new Among("ovec", 2, 1), new Among("ic", -1, 2),
			new Among("enic", 6, 3), new Among("och", -1, 1), new Among("\u00E1sek", -1, 1), new Among("nk", -1, 1),
			new Among("isk", -1, 2), new Among("ovisk", 11, 1), new Among("tk", -1, 1), new Among("vk", -1, 1),
			new Among("n\u00EDk", -1, 1), new Among("ovn\u00EDk", 15, 1), new Among("ov\u00EDk", -1, 1),
			new Among("\u010Dk", -1, 1), new Among("i\u0161k", -1, 2), new Among("u\u0161k", -1, 1),
			new Among("dl", -1, 1), new Among("itel", -1, 2), new Among("ul", -1, 1), new Among("an", -1, 1),
			new Among("\u010Dan", 24, 1), new Among("en", -1, 3), new Among("in", -1, 2), new Among("\u0161tin", 27, 1),
			new Among("ovin", 27, 1), new Among("teln", -1, 1), new Among("\u00E1rn", -1, 1),
			new Among("\u00EDrn", -1, 6), new Among("oun", -1, 1), new Among("loun", 33, 1), new Among("ovn", -1, 1),
			new Among("yn", -1, 1), new Among("kyn", 36, 1), new Among("\u00E1n", -1, 1), new Among("i\u00E1n", 38, 2),
			new Among("\u00EDn", -1, 6), new Among("\u010Dn", -1, 1), new Among("\u011Bn", -1, 5),
			new Among("as", -1, 1), new Among("it", -1, 2), new Among("ot", -1, 1), new Among("ist", -1, 2),
			new Among("ost", -1, 1), new Among("nost", 47, 1), new Among("out", -1, 1), new Among("ovi\u0161t", -1, 1),
			new Among("iv", -1, 2), new Among("ov", -1, 1), new Among("tv", -1, 1), new Among("ctv", 53, 1),
			new Among("stv", 53, 1), new Among("ovstv", 55, 1), new Among("ovtv", 53, 1), new Among("a\u010D", -1, 1),
			new Among("\u00E1\u010D", -1, 1), new Among("o\u0148", -1, 1), new Among("\u00E1\u0159", -1, 1),
			new Among("k\u00E1\u0159", 61, 1), new Among("ion\u00E1\u0159", 61, 2), new Among("\u00E9\u0159", -1, 4),
			new Among("n\u00E9\u0159", 64, 1), new Among("\u00ED\u0159", -1, 6), new Among("ou\u0161", -1, 1) };

	private final static Among a_4[] = { new Among("c", -1, 1), new Among("k", -1, 1), new Among("l", -1, 1),
			new Among("n", -1, 1), new Among("t", -1, 1), new Among("\u010D", -1, 1) };

	private final static Among a_5[] = { new Among("isk", -1, 2), new Among("\u00E1k", -1, 1), new Among("izn", -1, 2),
			new Among("ajzn", -1, 1) };

	private final static Among a_6[] = { new Among("k", -1, 1), new Among("ak", 0, 7), new Among("ek", 0, 2),
			new Among("anek", 2, 1), new Among("enek", 2, 2), new Among("inek", 2, 4), new Among("onek", 2, 1),
			new Among("unek", 2, 1), new Among("\u00E1nek", 2, 1), new Among("a\u010Dek", 2, 1),
			new Among("e\u010Dek", 2, 2), new Among("i\u010Dek", 2, 4), new Among("o\u010Dek", 2, 1),
			new Among("u\u010Dek", 2, 1), new Among("\u00E1\u010Dek", 2, 1), new Among("\u00E9\u010Dek", 2, 3),
			new Among("\u00ED\u010Dek", 2, 5), new Among("ou\u0161ek", 2, 1), new Among("ik", 0, 4),
			new Among("ank", 0, 1), new Among("enk", 0, 1), new Among("ink", 0, 1), new Among("onk", 0, 1),
			new Among("unk", 0, 1), new Among("\u00E1nk", 0, 1), new Among("\u00E9nk", 0, 1),
			new Among("\u00EDnk", 0, 1), new Among("ok", 0, 8), new Among("\u00E1tk", 0, 1), new Among("uk", 0, 9),
			new Among("\u00E1k", 0, 6), new Among("\u00E9k", 0, 3), new Among("\u00EDk", 0, 5),
			new Among("a\u010Dk", 0, 1), new Among("e\u010Dk", 0, 1), new Among("i\u010Dk", 0, 1),
			new Among("o\u010Dk", 0, 1), new Among("u\u010Dk", 0, 1), new Among("\u00E1\u010Dk", 0, 1),
			new Among("\u00E9\u010Dk", 0, 1), new Among("\u00ED\u010Dk", 0, 1), new Among("u\u0161k", 0, 1) };

	private final static Among a_7[] = { new Among("ej\u0161", -1, 2), new Among("\u011Bj\u0161", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 4, 18, 0, 0, 0, 4, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 64 };

	private int I_p1;
	private int I_pV;

	private boolean r_mark_regions() {
		int v_1;
		// (, line 43
		I_pV = limit;
		I_p1 = limit;
		// do, line 48
		v_1 = cursor;
		lab0: do {
			// (, line 48
			// gopast, line 49
			golab1: while (true) {
				lab2: do {
					if (!(out_grouping(g_v, 97, 367))) {
						break lab2;
					}
					break golab1;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark pV, line 49
			I_pV = cursor;
			// gopast, line 50
			golab3: while (true) {
				lab4: do {
					if (!(out_grouping(g_v, 97, 367))) {
						break lab4;
					}
					break golab3;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 50
			golab5: while (true) {
				lab6: do {
					if (!(in_grouping(g_v, 97, 367))) {
						break lab6;
					}
					break golab5;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark p1, line 50
			I_p1 = cursor;
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_palatalise() {
		int among_var;
		// (, line 59
		// [, line 60
		ket = cursor;
		// substring, line 60
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			return false;
		}
		// ], line 60
		bra = cursor;
		// call RV, line 60
		if (!r_RV()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 62
			// <-, line 62
			slice_from("k");
			break;
		case 2:
			// (, line 64
			// <-, line 64
			slice_from("h");
			break;
		case 3:
			// (, line 66
			// <-, line 66
			slice_from("ck");
			break;
		case 4:
			// (, line 68
			// <-, line 68
			slice_from("sk");
			break;
		}
		return true;
	}

	private boolean r_do_possessive() {
		int among_var;
		int v_1;
		// (, line 72
		// [, line 73
		ket = cursor;
		// substring, line 73
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 73
		bra = cursor;
		// call RV, line 73
		if (!r_RV()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 75
			// delete, line 75
			slice_del();
			break;
		case 2:
			// (, line 77
			// delete, line 78
			slice_del();
			// try, line 79
			v_1 = limit - cursor;
			lab0: do {
				// call palatalise, line 79
				if (!r_palatalise()) {
					cursor = limit - v_1;
					break lab0;
				}
			} while (false);
			break;
		}
		return true;
	}

	private boolean r_do_case() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 84
		// [, line 85
		ket = cursor;
		// substring, line 85
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 85
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 92
			// delete, line 92
			slice_del();
			break;
		case 2:
			// (, line 98
			// delete, line 99
			slice_del();
			// try, line 100
			v_1 = limit - cursor;
			lab0: do {
				// call palatalise, line 100
				if (!r_palatalise()) {
					cursor = limit - v_1;
					break lab0;
				}
			} while (false);
			break;
		case 3:
			// (, line 103
			// <-, line 104
			slice_from("e");
			// try, line 105
			v_2 = limit - cursor;
			lab1: do {
				// call palatalise, line 105
				if (!r_palatalise()) {
					cursor = limit - v_2;
					break lab1;
				}
			} while (false);
			break;
		}
		return true;
	}

	private boolean r_do_derivational() {
		int among_var;
		// (, line 110
		// [, line 111
		ket = cursor;
		// substring, line 111
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 111
		bra = cursor;
		// call R1, line 111
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 120
			// delete, line 120
			slice_del();
			break;
		case 2:
			// (, line 125
			// <-, line 126
			slice_from("i");
			// call palatalise, line 127
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 3:
			// (, line 130
			// <-, line 131
			slice_from("e");
			// call palatalise, line 132
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 4:
			// (, line 135
			// <-, line 136
			slice_from("\u00E9");
			// call palatalise, line 137
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 5:
			// (, line 140
			// <-, line 141
			slice_from("\u011B");
			// call palatalise, line 142
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 6:
			// (, line 146
			// <-, line 147
			slice_from("\u00ED");
			// call palatalise, line 148
			if (!r_palatalise()) {
				return false;
			}
			break;
		}
		return true;
	}

	private boolean r_do_deriv_single() {
		int among_var;
		// (, line 152
		// [, line 153
		ket = cursor;
		// substring, line 153
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 153
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 155
			// delete, line 155
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_do_augmentative() {
		int among_var;
		// (, line 160
		// [, line 161
		ket = cursor;
		// substring, line 161
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 161
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 163
			// delete, line 163
			slice_del();
			break;
		case 2:
			// (, line 165
			// <-, line 166
			slice_from("i");
			// call palatalise, line 167
			if (!r_palatalise()) {
				return false;
			}
			break;
		}
		return true;
	}

	private boolean r_do_diminutive() {
		int among_var;
		// (, line 172
		// [, line 173
		ket = cursor;
		// substring, line 173
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 173
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 180
			// delete, line 180
			slice_del();
			break;
		case 2:
			// (, line 182
			// <-, line 183
			slice_from("e");
			// call palatalise, line 184
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 3:
			// (, line 187
			// <-, line 188
			slice_from("\u00E9");
			// call palatalise, line 189
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 4:
			// (, line 192
			// <-, line 193
			slice_from("i");
			// call palatalise, line 194
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 5:
			// (, line 197
			// <-, line 198
			slice_from("\u00ED");
			// call palatalise, line 199
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 6:
			// (, line 202
			// <-, line 202
			slice_from("\u00E1");
			break;
		case 7:
			// (, line 204
			// <-, line 204
			slice_from("a");
			break;
		case 8:
			// (, line 206
			// <-, line 206
			slice_from("o");
			break;
		case 9:
			// (, line 208
			// <-, line 208
			slice_from("u");
			break;
		}
		return true;
	}

	private boolean r_do_comparative() {
		int among_var;
		// (, line 212
		// [, line 213
		ket = cursor;
		// substring, line 213
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			return false;
		}
		// ], line 213
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 215
			// <-, line 216
			slice_from("\u011B");
			// call palatalise, line 217
			if (!r_palatalise()) {
				return false;
			}
			break;
		case 2:
			// (, line 220
			// <-, line 221
			slice_from("e");
			// call palatalise, line 222
			if (!r_palatalise()) {
				return false;
			}
			break;
		}
		return true;
	}

	private boolean r_do_aggressive() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 227
		// do, line 228
		v_1 = limit - cursor;
		lab0: do {
			// call do_comparative, line 228
			if (!r_do_comparative()) {
				break lab0;
			}
		} while (false);
		cursor = limit - v_1;
		// do, line 229
		v_2 = limit - cursor;
		lab1: do {
			// call do_diminutive, line 229
			if (!r_do_diminutive()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 230
		v_3 = limit - cursor;
		lab2: do {
			// call do_augmentative, line 230
			if (!r_do_augmentative()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// or, line 231
		lab3: do {
			v_4 = limit - cursor;
			lab4: do {
				// call do_derivational, line 231
				if (!r_do_derivational()) {
					break lab4;
				}
				break lab3;
			} while (false);
			cursor = limit - v_4;
			// call do_deriv_single, line 231
			if (!r_do_deriv_single()) {
				return false;
			}
		} while (false);
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		// (, line 235
		// do, line 236
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 236
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 237
		limit_backward = cursor;
		cursor = limit;
		// (, line 237
		// call do_case, line 238
		if (!r_do_case()) {
			return false;
		}
		// call do_possessive, line 239
		if (!r_do_possessive()) {
			return false;
		}
		// call do_aggressive, line 242
		if (!r_do_aggressive()) {
			return false;
		}
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class danishStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("hed", -1, 1), new Among("ethed", 0, 1), new Among("ered", -1, 1),
			new Among("e", -1, 1), new Among("erede", 3, 1), new Among("ende", 3, 1), new Among("erende", 5, 1),
			new Among("ene", 3, 1), new Among("erne", 3, 1), new Among("ere", 3, 1), new Among("en", -1, 1),
			new Among("heden", 10, 1), new Among("eren", 10, 1), new Among("er", -1, 1), new Among("heder", 13, 1),
			new Among("erer", 13, 1), new Among("s", -1, 2), new Among("heds", 16, 1), new Among("es", 16, 1),
			new Among("endes", 18, 1), new Among("erendes", 19, 1), new Among("enes", 18, 1), new Among("ernes", 18, 1),
			new Among("eres", 18, 1), new Among("ens", 16, 1), new Among("hedens", 24, 1), new Among("erens", 24, 1),
			new Among("ers", 16, 1), new Among("ets", 16, 1), new Among("erets", 28, 1), new Among("et", -1, 1),
			new Among("eret", 30, 1) };

	private final static Among a_1[] = { new Among("gd", -1, -1), new Among("dt", -1, -1), new Among("gt", -1, -1),
			new Among("kt", -1, -1) };

	private final static Among a_2[] = { new Among("ig", -1, 1), new Among("lig", 0, 1), new Among("elig", 1, 1),
			new Among("els", -1, 1), new Among("l\u00F8st", -1, 2) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 128 };

	private static final char g_s_ending[] = { 239, 254, 42, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16 };

	private int I_x;
	private int I_p1;
	private StringBuilder S_ch = new StringBuilder();

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		// (, line 29
		I_p1 = limit;
		// test, line 33
		v_1 = cursor;
		// (, line 33
		// hop, line 33
		{
			int c = cursor + 3;
			if (0 > c || c > limit) {
				return false;
			}
			cursor = c;
		}
		// setmark x, line 33
		I_x = cursor;
		cursor = v_1;
		// goto, line 34
		golab0: while (true) {
			v_2 = cursor;
			lab1: do {
				if (!(in_grouping(g_v, 97, 248))) {
					break lab1;
				}
				cursor = v_2;
				break golab0;
			} while (false);
			cursor = v_2;
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 34
		golab2: while (true) {
			lab3: do {
				if (!(out_grouping(g_v, 97, 248))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p1, line 34
		I_p1 = cursor;
		// try, line 35
		lab4: do {
			// (, line 35
			if (!(I_p1 < I_x)) {
				break lab4;
			}
			I_p1 = I_x;
		} while (false);
		return true;
	}

	private boolean r_main_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 40
		// setlimit, line 41
		v_1 = limit - cursor;
		// tomark, line 41
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 41
		// [, line 41
		ket = cursor;
		// substring, line 41
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 41
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 48
			// delete, line 48
			slice_del();
			break;
		case 2:
			// (, line 50
			if (!(in_grouping_b(g_s_ending, 97, 229))) {
				return false;
			}
			// delete, line 50
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_consonant_pair() {
		int v_1;
		int v_2;
		int v_3;
		// (, line 54
		// test, line 55
		v_1 = limit - cursor;
		// (, line 55
		// setlimit, line 56
		v_2 = limit - cursor;
		// tomark, line 56
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_3 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_2;
		// (, line 56
		// [, line 56
		ket = cursor;
		// substring, line 56
		if (find_among_b(a_1) == 0) {
			limit_backward = v_3;
			return false;
		}
		// ], line 56
		bra = cursor;
		limit_backward = v_3;
		cursor = limit - v_1;
		// next, line 62
		if (cursor <= limit_backward) {
			return false;
		}
		cursor--;
		// ], line 62
		bra = cursor;
		// delete, line 62
		slice_del();
		return true;
	}

	private boolean r_other_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 65
		// do, line 66
		v_1 = limit - cursor;
		lab0: do {
			// (, line 66
			// [, line 66
			ket = cursor;
			// literal, line 66
			if (!(eq_s_b("st"))) {
				break lab0;
			}
			// ], line 66
			bra = cursor;
			// literal, line 66
			if (!(eq_s_b("ig"))) {
				break lab0;
			}
			// delete, line 66
			slice_del();
		} while (false);
		cursor = limit - v_1;
		// setlimit, line 67
		v_2 = limit - cursor;
		// tomark, line 67
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_3 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_2;
		// (, line 67
		// [, line 67
		ket = cursor;
		// substring, line 67
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			limit_backward = v_3;
			return false;
		}
		// ], line 67
		bra = cursor;
		limit_backward = v_3;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 70
			// delete, line 70
			slice_del();
			// do, line 70
			v_4 = limit - cursor;
			lab1: do {
				// call consonant_pair, line 70
				if (!r_consonant_pair()) {
					break lab1;
				}
			} while (false);
			cursor = limit - v_4;
			break;
		case 2:
			// (, line 72
			// <-, line 72
			slice_from("l\u00F8s");
			break;
		}
		return true;
	}

	private boolean r_undouble() {
		int v_1;
		int v_2;
		// (, line 75
		// setlimit, line 76
		v_1 = limit - cursor;
		// tomark, line 76
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 76
		// [, line 76
		ket = cursor;
		if (!(out_grouping_b(g_v, 97, 248))) {
			limit_backward = v_2;
			return false;
		}
		// ], line 76
		bra = cursor;
		// -> ch, line 76
		S_ch = slice_to(S_ch);
		limit_backward = v_2;
		// name ch, line 77
		if (!(eq_s_b(S_ch.toString()))) {
			return false;
		}
		// delete, line 78
		slice_del();
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 82
		// do, line 84
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 84
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 85
		limit_backward = cursor;
		cursor = limit;
		// (, line 85
		// do, line 86
		v_2 = limit - cursor;
		lab1: do {
			// call main_suffix, line 86
			if (!r_main_suffix()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 87
		v_3 = limit - cursor;
		lab2: do {
			// call consonant_pair, line 87
			if (!r_consonant_pair()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 88
		v_4 = limit - cursor;
		lab3: do {
			// call other_suffix, line 88
			if (!r_other_suffix()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		// do, line 89
		v_5 = limit - cursor;
		lab4: do {
			// call undouble, line 89
			if (!r_undouble()) {
				break lab4;
			}
		} while (false);
		cursor = limit - v_5;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class dutchStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 6), new Among("\u00E1", 0, 1), new Among("\u00E4", 0, 1),
			new Among("\u00E9", 0, 2), new Among("\u00EB", 0, 2), new Among("\u00ED", 0, 3), new Among("\u00EF", 0, 3),
			new Among("\u00F3", 0, 4), new Among("\u00F6", 0, 4), new Among("\u00FA", 0, 5),
			new Among("\u00FC", 0, 5) };

	private final static Among a_1[] = { new Among("", -1, 3), new Among("I", 0, 2), new Among("Y", 0, 1) };

	private final static Among a_2[] = { new Among("dd", -1, -1), new Among("kk", -1, -1), new Among("tt", -1, -1) };

	private final static Among a_3[] = { new Among("ene", -1, 2), new Among("se", -1, 3), new Among("en", -1, 2),
			new Among("heden", 2, 1), new Among("s", -1, 3) };

	private final static Among a_4[] = { new Among("end", -1, 1), new Among("ig", -1, 2), new Among("ing", -1, 1),
			new Among("lijk", -1, 3), new Among("baar", -1, 4), new Among("bar", -1, 5) };

	private final static Among a_5[] = { new Among("aa", -1, -1), new Among("ee", -1, -1), new Among("oo", -1, -1),
			new Among("uu", -1, -1) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };

	private static final char g_v_I[] = { 1, 0, 0, 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };

	private static final char g_v_j[] = { 17, 67, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };

	private int I_p2;
	private int I_p1;
	private boolean B_e_found;

	private boolean r_prelude() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		// (, line 41
		// test, line 42
		v_1 = cursor;
		// repeat, line 42
		replab0: while (true) {
			v_2 = cursor;
			lab1: do {
				// (, line 42
				// [, line 43
				bra = cursor;
				// substring, line 43
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 43
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 45
					// <-, line 45
					slice_from("a");
					break;
				case 2:
					// (, line 47
					// <-, line 47
					slice_from("e");
					break;
				case 3:
					// (, line 49
					// <-, line 49
					slice_from("i");
					break;
				case 4:
					// (, line 51
					// <-, line 51
					slice_from("o");
					break;
				case 5:
					// (, line 53
					// <-, line 53
					slice_from("u");
					break;
				case 6:
					// (, line 54
					// next, line 54
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_2;
			break replab0;
		}
		cursor = v_1;
		// try, line 57
		v_3 = cursor;
		lab2: do {
			// (, line 57
			// [, line 57
			bra = cursor;
			// literal, line 57
			if (!(eq_s("y"))) {
				cursor = v_3;
				break lab2;
			}
			// ], line 57
			ket = cursor;
			// <-, line 57
			slice_from("Y");
		} while (false);
		// repeat, line 58
		replab3: while (true) {
			v_4 = cursor;
			lab4: do {
				// goto, line 58
				golab5: while (true) {
					v_5 = cursor;
					lab6: do {
						// (, line 58
						if (!(in_grouping(g_v, 97, 232))) {
							break lab6;
						}
						// [, line 59
						bra = cursor;
						// or, line 59
						lab7: do {
							v_6 = cursor;
							lab8: do {
								// (, line 59
								// literal, line 59
								if (!(eq_s("i"))) {
									break lab8;
								}
								// ], line 59
								ket = cursor;
								if (!(in_grouping(g_v, 97, 232))) {
									break lab8;
								}
								// <-, line 59
								slice_from("I");
								break lab7;
							} while (false);
							cursor = v_6;
							// (, line 60
							// literal, line 60
							if (!(eq_s("y"))) {
								break lab6;
							}
							// ], line 60
							ket = cursor;
							// <-, line 60
							slice_from("Y");
						} while (false);
						cursor = v_5;
						break golab5;
					} while (false);
					cursor = v_5;
					if (cursor >= limit) {
						break lab4;
					}
					cursor++;
				}
				continue replab3;
			} while (false);
			cursor = v_4;
			break replab3;
		}
		return true;
	}

	private boolean r_mark_regions() {
		// (, line 64
		I_p1 = limit;
		I_p2 = limit;
		// gopast, line 69
		golab0: while (true) {
			lab1: do {
				if (!(in_grouping(g_v, 97, 232))) {
					break lab1;
				}
				break golab0;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 69
		golab2: while (true) {
			lab3: do {
				if (!(out_grouping(g_v, 97, 232))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p1, line 69
		I_p1 = cursor;
		// try, line 70
		lab4: do {
			// (, line 70
			if (!(I_p1 < 3)) {
				break lab4;
			}
			I_p1 = 3;
		} while (false);
		// gopast, line 71
		golab5: while (true) {
			lab6: do {
				if (!(in_grouping(g_v, 97, 232))) {
					break lab6;
				}
				break golab5;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 71
		golab7: while (true) {
			lab8: do {
				if (!(out_grouping(g_v, 97, 232))) {
					break lab8;
				}
				break golab7;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p2, line 71
		I_p2 = cursor;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 75
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 75
				// [, line 77
				bra = cursor;
				// substring, line 77
				among_var = find_among(a_1);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 77
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 78
					// <-, line 78
					slice_from("y");
					break;
				case 2:
					// (, line 79
					// <-, line 79
					slice_from("i");
					break;
				case 3:
					// (, line 80
					// next, line 80
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_undouble() {
		int v_1;
		// (, line 90
		// test, line 91
		v_1 = limit - cursor;
		// among, line 91
		if (find_among_b(a_2) == 0) {
			return false;
		}
		cursor = limit - v_1;
		// [, line 91
		ket = cursor;
		// next, line 91
		if (cursor <= limit_backward) {
			return false;
		}
		cursor--;
		// ], line 91
		bra = cursor;
		// delete, line 91
		slice_del();
		return true;
	}

	private boolean r_e_ending() {
		int v_1;
		// (, line 94
		// unset e_found, line 95
		B_e_found = false;
		// [, line 96
		ket = cursor;
		// literal, line 96
		if (!(eq_s_b("e"))) {
			return false;
		}
		// ], line 96
		bra = cursor;
		// call R1, line 96
		if (!r_R1()) {
			return false;
		}
		// test, line 96
		v_1 = limit - cursor;
		if (!(out_grouping_b(g_v, 97, 232))) {
			return false;
		}
		cursor = limit - v_1;
		// delete, line 96
		slice_del();
		// set e_found, line 97
		B_e_found = true;
		// call undouble, line 98
		if (!r_undouble()) {
			return false;
		}
		return true;
	}

	private boolean r_en_ending() {
		int v_1;
		int v_2;
		// (, line 101
		// call R1, line 102
		if (!r_R1()) {
			return false;
		}
		// and, line 102
		v_1 = limit - cursor;
		if (!(out_grouping_b(g_v, 97, 232))) {
			return false;
		}
		cursor = limit - v_1;
		// not, line 102
		{
			v_2 = limit - cursor;
			lab0: do {
				// literal, line 102
				if (!(eq_s_b("gem"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = limit - v_2;
		}
		// delete, line 102
		slice_del();
		// call undouble, line 103
		if (!r_undouble()) {
			return false;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 106
		// do, line 107
		v_1 = limit - cursor;
		lab0: do {
			// (, line 107
			// [, line 108
			ket = cursor;
			// substring, line 108
			among_var = find_among_b(a_3);
			if (among_var == 0) {
				break lab0;
			}
			// ], line 108
			bra = cursor;
			switch (among_var) {
			case 0:
				break lab0;
			case 1:
				// (, line 110
				// call R1, line 110
				if (!r_R1()) {
					break lab0;
				}
				// <-, line 110
				slice_from("heid");
				break;
			case 2:
				// (, line 113
				// call en_ending, line 113
				if (!r_en_ending()) {
					break lab0;
				}
				break;
			case 3:
				// (, line 116
				// call R1, line 116
				if (!r_R1()) {
					break lab0;
				}
				if (!(out_grouping_b(g_v_j, 97, 232))) {
					break lab0;
				}
				// delete, line 116
				slice_del();
				break;
			}
		} while (false);
		cursor = limit - v_1;
		// do, line 120
		v_2 = limit - cursor;
		lab1: do {
			// call e_ending, line 120
			if (!r_e_ending()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 122
		v_3 = limit - cursor;
		lab2: do {
			// (, line 122
			// [, line 122
			ket = cursor;
			// literal, line 122
			if (!(eq_s_b("heid"))) {
				break lab2;
			}
			// ], line 122
			bra = cursor;
			// call R2, line 122
			if (!r_R2()) {
				break lab2;
			}
			// not, line 122
			{
				v_4 = limit - cursor;
				lab3: do {
					// literal, line 122
					if (!(eq_s_b("c"))) {
						break lab3;
					}
					break lab2;
				} while (false);
				cursor = limit - v_4;
			}
			// delete, line 122
			slice_del();
			// [, line 123
			ket = cursor;
			// literal, line 123
			if (!(eq_s_b("en"))) {
				break lab2;
			}
			// ], line 123
			bra = cursor;
			// call en_ending, line 123
			if (!r_en_ending()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 126
		v_5 = limit - cursor;
		lab4: do {
			// (, line 126
			// [, line 127
			ket = cursor;
			// substring, line 127
			among_var = find_among_b(a_4);
			if (among_var == 0) {
				break lab4;
			}
			// ], line 127
			bra = cursor;
			switch (among_var) {
			case 0:
				break lab4;
			case 1:
				// (, line 129
				// call R2, line 129
				if (!r_R2()) {
					break lab4;
				}
				// delete, line 129
				slice_del();
				// or, line 130
				lab5: do {
					v_6 = limit - cursor;
					lab6: do {
						// (, line 130
						// [, line 130
						ket = cursor;
						// literal, line 130
						if (!(eq_s_b("ig"))) {
							break lab6;
						}
						// ], line 130
						bra = cursor;
						// call R2, line 130
						if (!r_R2()) {
							break lab6;
						}
						// not, line 130
						{
							v_7 = limit - cursor;
							lab7: do {
								// literal, line 130
								if (!(eq_s_b("e"))) {
									break lab7;
								}
								break lab6;
							} while (false);
							cursor = limit - v_7;
						}
						// delete, line 130
						slice_del();
						break lab5;
					} while (false);
					cursor = limit - v_6;
					// call undouble, line 130
					if (!r_undouble()) {
						break lab4;
					}
				} while (false);
				break;
			case 2:
				// (, line 133
				// call R2, line 133
				if (!r_R2()) {
					break lab4;
				}
			// not, line 133
			{
				v_8 = limit - cursor;
				lab8: do {
					// literal, line 133
					if (!(eq_s_b("e"))) {
						break lab8;
					}
					break lab4;
				} while (false);
				cursor = limit - v_8;
			}
				// delete, line 133
				slice_del();
				break;
			case 3:
				// (, line 136
				// call R2, line 136
				if (!r_R2()) {
					break lab4;
				}
				// delete, line 136
				slice_del();
				// call e_ending, line 136
				if (!r_e_ending()) {
					break lab4;
				}
				break;
			case 4:
				// (, line 139
				// call R2, line 139
				if (!r_R2()) {
					break lab4;
				}
				// delete, line 139
				slice_del();
				break;
			case 5:
				// (, line 142
				// call R2, line 142
				if (!r_R2()) {
					break lab4;
				}
				// Boolean test e_found, line 142
				if (!(B_e_found)) {
					break lab4;
				}
				// delete, line 142
				slice_del();
				break;
			}
		} while (false);
		cursor = limit - v_5;
		// do, line 146
		v_9 = limit - cursor;
		lab9: do {
			// (, line 146
			if (!(out_grouping_b(g_v_I, 73, 232))) {
				break lab9;
			}
			// test, line 148
			v_10 = limit - cursor;
			// (, line 148
			// among, line 149
			if (find_among_b(a_5) == 0) {
				break lab9;
			}
			if (!(out_grouping_b(g_v, 97, 232))) {
				break lab9;
			}
			cursor = limit - v_10;
			// [, line 152
			ket = cursor;
			// next, line 152
			if (cursor <= limit_backward) {
				break lab9;
			}
			cursor--;
			// ], line 152
			bra = cursor;
			// delete, line 152
			slice_del();
		} while (false);
		cursor = limit - v_9;
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 157
		// do, line 159
		v_1 = cursor;
		lab0: do {
			// call prelude, line 159
			if (!r_prelude()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 160
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 160
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 161
		limit_backward = cursor;
		cursor = limit;
		// do, line 162
		v_3 = limit - cursor;
		lab2: do {
			// call standard_suffix, line 162
			if (!r_standard_suffix()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		cursor = limit_backward; // do, line 163
		v_4 = cursor;
		lab3: do {
			// call postlude, line 163
			if (!r_postlude()) {
				break lab3;
			}
		} while (false);
		cursor = v_4;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class englishStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("arsen", -1, -1), new Among("commun", -1, -1),
			new Among("gener", -1, -1) };

	private final static Among a_1[] = { new Among("'", -1, 1), new Among("'s'", 0, 1), new Among("'s", -1, 1) };

	private final static Among a_2[] = { new Among("ied", -1, 2), new Among("s", -1, 3), new Among("ies", 1, 2),
			new Among("sses", 1, 1), new Among("ss", 1, -1), new Among("us", 1, -1) };

	private final static Among a_3[] = { new Among("", -1, 3), new Among("bb", 0, 2), new Among("dd", 0, 2),
			new Among("ff", 0, 2), new Among("gg", 0, 2), new Among("bl", 0, 1), new Among("mm", 0, 2),
			new Among("nn", 0, 2), new Among("pp", 0, 2), new Among("rr", 0, 2), new Among("at", 0, 1),
			new Among("tt", 0, 2), new Among("iz", 0, 1) };

	private final static Among a_4[] = { new Among("ed", -1, 2), new Among("eed", 0, 1), new Among("ing", -1, 2),
			new Among("edly", -1, 2), new Among("eedly", 3, 1), new Among("ingly", -1, 2) };

	private final static Among a_5[] = { new Among("anci", -1, 3), new Among("enci", -1, 2), new Among("ogi", -1, 13),
			new Among("li", -1, 16), new Among("bli", 3, 12), new Among("abli", 4, 4), new Among("alli", 3, 8),
			new Among("fulli", 3, 14), new Among("lessli", 3, 15), new Among("ousli", 3, 10), new Among("entli", 3, 5),
			new Among("aliti", -1, 8), new Among("biliti", -1, 12), new Among("iviti", -1, 11),
			new Among("tional", -1, 1), new Among("ational", 14, 7), new Among("alism", -1, 8),
			new Among("ation", -1, 7), new Among("ization", 17, 6), new Among("izer", -1, 6), new Among("ator", -1, 7),
			new Among("iveness", -1, 11), new Among("fulness", -1, 9), new Among("ousness", -1, 10) };

	private final static Among a_6[] = { new Among("icate", -1, 4), new Among("ative", -1, 6),
			new Among("alize", -1, 3), new Among("iciti", -1, 4), new Among("ical", -1, 4), new Among("tional", -1, 1),
			new Among("ational", 5, 2), new Among("ful", -1, 5), new Among("ness", -1, 5) };

	private final static Among a_7[] = { new Among("ic", -1, 1), new Among("ance", -1, 1), new Among("ence", -1, 1),
			new Among("able", -1, 1), new Among("ible", -1, 1), new Among("ate", -1, 1), new Among("ive", -1, 1),
			new Among("ize", -1, 1), new Among("iti", -1, 1), new Among("al", -1, 1), new Among("ism", -1, 1),
			new Among("ion", -1, 2), new Among("er", -1, 1), new Among("ous", -1, 1), new Among("ant", -1, 1),
			new Among("ent", -1, 1), new Among("ment", 15, 1), new Among("ement", 16, 1) };

	private final static Among a_8[] = { new Among("e", -1, 1), new Among("l", -1, 2) };

	private final static Among a_9[] = { new Among("succeed", -1, -1), new Among("proceed", -1, -1),
			new Among("exceed", -1, -1), new Among("canning", -1, -1), new Among("inning", -1, -1),
			new Among("earring", -1, -1), new Among("herring", -1, -1), new Among("outing", -1, -1) };

	private final static Among a_10[] = { new Among("andes", -1, -1), new Among("atlas", -1, -1),
			new Among("bias", -1, -1), new Among("cosmos", -1, -1), new Among("dying", -1, 3),
			new Among("early", -1, 9), new Among("gently", -1, 7), new Among("howe", -1, -1), new Among("idly", -1, 6),
			new Among("lying", -1, 4), new Among("news", -1, -1), new Among("only", -1, 10),
			new Among("singly", -1, 11), new Among("skies", -1, 2), new Among("skis", -1, 1), new Among("sky", -1, -1),
			new Among("tying", -1, 5), new Among("ugly", -1, 8) };

	private static final char g_v[] = { 17, 65, 16, 1 };

	private static final char g_v_WXY[] = { 1, 17, 65, 208, 1 };

	private static final char g_valid_LI[] = { 55, 141, 2 };

	private boolean B_Y_found;
	private int I_p2;
	private int I_p1;

	private boolean r_prelude() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 25
		// unset Y_found, line 26
		B_Y_found = false;
		// do, line 27
		v_1 = cursor;
		lab0: do {
			// (, line 27
			// [, line 27
			bra = cursor;
			// literal, line 27
			if (!(eq_s("'"))) {
				break lab0;
			}
			// ], line 27
			ket = cursor;
			// delete, line 27
			slice_del();
		} while (false);
		cursor = v_1;
		// do, line 28
		v_2 = cursor;
		lab1: do {
			// (, line 28
			// [, line 28
			bra = cursor;
			// literal, line 28
			if (!(eq_s("y"))) {
				break lab1;
			}
			// ], line 28
			ket = cursor;
			// <-, line 28
			slice_from("Y");
			// set Y_found, line 28
			B_Y_found = true;
		} while (false);
		cursor = v_2;
		// do, line 29
		v_3 = cursor;
		lab2: do {
			// repeat, line 29
			replab3: while (true) {
				v_4 = cursor;
				lab4: do {
					// (, line 29
					// goto, line 29
					golab5: while (true) {
						v_5 = cursor;
						lab6: do {
							// (, line 29
							if (!(in_grouping(g_v, 97, 121))) {
								break lab6;
							}
							// [, line 29
							bra = cursor;
							// literal, line 29
							if (!(eq_s("y"))) {
								break lab6;
							}
							// ], line 29
							ket = cursor;
							cursor = v_5;
							break golab5;
						} while (false);
						cursor = v_5;
						if (cursor >= limit) {
							break lab4;
						}
						cursor++;
					}
					// <-, line 29
					slice_from("Y");
					// set Y_found, line 29
					B_Y_found = true;
					continue replab3;
				} while (false);
				cursor = v_4;
				break replab3;
			}
		} while (false);
		cursor = v_3;
		return true;
	}

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		// (, line 32
		I_p1 = limit;
		I_p2 = limit;
		// do, line 35
		v_1 = cursor;
		lab0: do {
			// (, line 35
			// or, line 41
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// among, line 36
					if (find_among(a_0) == 0) {
						break lab2;
					}
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 41
				// gopast, line 41
				golab3: while (true) {
					lab4: do {
						if (!(in_grouping(g_v, 97, 121))) {
							break lab4;
						}
						break golab3;
					} while (false);
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				}
				// gopast, line 41
				golab5: while (true) {
					lab6: do {
						if (!(out_grouping(g_v, 97, 121))) {
							break lab6;
						}
						break golab5;
					} while (false);
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				}
			} while (false);
			// setmark p1, line 42
			I_p1 = cursor;
			// gopast, line 43
			golab7: while (true) {
				lab8: do {
					if (!(in_grouping(g_v, 97, 121))) {
						break lab8;
					}
					break golab7;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 43
			golab9: while (true) {
				lab10: do {
					if (!(out_grouping(g_v, 97, 121))) {
						break lab10;
					}
					break golab9;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark p2, line 43
			I_p2 = cursor;
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_shortv() {
		int v_1;
		// (, line 49
		// or, line 51
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 50
				if (!(out_grouping_b(g_v_WXY, 89, 121))) {
					break lab1;
				}
				if (!(in_grouping_b(g_v, 97, 121))) {
					break lab1;
				}
				if (!(out_grouping_b(g_v, 97, 121))) {
					break lab1;
				}
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 52
			if (!(out_grouping_b(g_v, 97, 121))) {
				return false;
			}
			if (!(in_grouping_b(g_v, 97, 121))) {
				return false;
			}
			// atlimit, line 52
			if (cursor > limit_backward) {
				return false;
			}
		} while (false);
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_Step_1a() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 58
		// try, line 59
		v_1 = limit - cursor;
		lab0: do {
			// (, line 59
			// [, line 60
			ket = cursor;
			// substring, line 60
			among_var = find_among_b(a_1);
			if (among_var == 0) {
				cursor = limit - v_1;
				break lab0;
			}
			// ], line 60
			bra = cursor;
			switch (among_var) {
			case 0:
				cursor = limit - v_1;
				break lab0;
			case 1:
				// (, line 62
				// delete, line 62
				slice_del();
				break;
			}
		} while (false);
		// [, line 65
		ket = cursor;
		// substring, line 65
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 65
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 66
			// <-, line 66
			slice_from("ss");
			break;
		case 2:
			// (, line 68
			// or, line 68
			lab1: do {
				v_2 = limit - cursor;
				lab2: do {
					// (, line 68
					// hop, line 68
					{
						int c = cursor - 2;
						if (limit_backward > c || c > limit) {
							break lab2;
						}
						cursor = c;
					}
					// <-, line 68
					slice_from("i");
					break lab1;
				} while (false);
				cursor = limit - v_2;
				// <-, line 68
				slice_from("ie");
			} while (false);
			break;
		case 3:
			// (, line 69
			// next, line 69
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
			// gopast, line 69
			golab3: while (true) {
				lab4: do {
					if (!(in_grouping_b(g_v, 97, 121))) {
						break lab4;
					}
					break golab3;
				} while (false);
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
			}
			// delete, line 69
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_1b() {
		int among_var;
		int v_1;
		int v_3;
		int v_4;
		// (, line 74
		// [, line 75
		ket = cursor;
		// substring, line 75
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 75
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 77
			// call R1, line 77
			if (!r_R1()) {
				return false;
			}
			// <-, line 77
			slice_from("ee");
			break;
		case 2:
			// (, line 79
			// test, line 80
			v_1 = limit - cursor;
			// gopast, line 80
			golab0: while (true) {
				lab1: do {
					if (!(in_grouping_b(g_v, 97, 121))) {
						break lab1;
					}
					break golab0;
				} while (false);
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
			}
			cursor = limit - v_1;
			// delete, line 80
			slice_del();
			// test, line 81
			v_3 = limit - cursor;
			// substring, line 81
			among_var = find_among_b(a_3);
			if (among_var == 0) {
				return false;
			}
			cursor = limit - v_3;
			switch (among_var) {
			case 0:
				return false;
			case 1:
			// (, line 83
			// <+, line 83
			{
				int c = cursor;
				insert(cursor, cursor, "e");
				cursor = c;
			}
				break;
			case 2:
				// (, line 86
				// [, line 86
				ket = cursor;
				// next, line 86
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
				// ], line 86
				bra = cursor;
				// delete, line 86
				slice_del();
				break;
			case 3:
				// (, line 87
				// atmark, line 87
				if (cursor != I_p1) {
					return false;
				}
				// test, line 87
				v_4 = limit - cursor;
				// call shortv, line 87
				if (!r_shortv()) {
					return false;
				}
				cursor = limit - v_4;
			// <+, line 87
			{
				int c = cursor;
				insert(cursor, cursor, "e");
				cursor = c;
			}
				break;
			}
			break;
		}
		return true;
	}

	private boolean r_Step_1c() {
		int v_1;
		int v_2;
		// (, line 93
		// [, line 94
		ket = cursor;
		// or, line 94
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// literal, line 94
				if (!(eq_s_b("y"))) {
					break lab1;
				}
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// literal, line 94
			if (!(eq_s_b("Y"))) {
				return false;
			}
		} while (false);
		// ], line 94
		bra = cursor;
		if (!(out_grouping_b(g_v, 97, 121))) {
			return false;
		}
		// not, line 95
		{
			v_2 = limit - cursor;
			lab2: do {
				// atlimit, line 95
				if (cursor > limit_backward) {
					break lab2;
				}
				return false;
			} while (false);
			cursor = limit - v_2;
		}
		// <-, line 96
		slice_from("i");
		return true;
	}

	private boolean r_Step_2() {
		int among_var;
		// (, line 99
		// [, line 100
		ket = cursor;
		// substring, line 100
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 100
		bra = cursor;
		// call R1, line 100
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 101
			// <-, line 101
			slice_from("tion");
			break;
		case 2:
			// (, line 102
			// <-, line 102
			slice_from("ence");
			break;
		case 3:
			// (, line 103
			// <-, line 103
			slice_from("ance");
			break;
		case 4:
			// (, line 104
			// <-, line 104
			slice_from("able");
			break;
		case 5:
			// (, line 105
			// <-, line 105
			slice_from("ent");
			break;
		case 6:
			// (, line 107
			// <-, line 107
			slice_from("ize");
			break;
		case 7:
			// (, line 109
			// <-, line 109
			slice_from("ate");
			break;
		case 8:
			// (, line 111
			// <-, line 111
			slice_from("al");
			break;
		case 9:
			// (, line 112
			// <-, line 112
			slice_from("ful");
			break;
		case 10:
			// (, line 114
			// <-, line 114
			slice_from("ous");
			break;
		case 11:
			// (, line 116
			// <-, line 116
			slice_from("ive");
			break;
		case 12:
			// (, line 118
			// <-, line 118
			slice_from("ble");
			break;
		case 13:
			// (, line 119
			// literal, line 119
			if (!(eq_s_b("l"))) {
				return false;
			}
			// <-, line 119
			slice_from("og");
			break;
		case 14:
			// (, line 120
			// <-, line 120
			slice_from("ful");
			break;
		case 15:
			// (, line 121
			// <-, line 121
			slice_from("less");
			break;
		case 16:
			// (, line 122
			if (!(in_grouping_b(g_valid_LI, 99, 116))) {
				return false;
			}
			// delete, line 122
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_3() {
		int among_var;
		// (, line 126
		// [, line 127
		ket = cursor;
		// substring, line 127
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 127
		bra = cursor;
		// call R1, line 127
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 128
			// <-, line 128
			slice_from("tion");
			break;
		case 2:
			// (, line 129
			// <-, line 129
			slice_from("ate");
			break;
		case 3:
			// (, line 130
			// <-, line 130
			slice_from("al");
			break;
		case 4:
			// (, line 132
			// <-, line 132
			slice_from("ic");
			break;
		case 5:
			// (, line 134
			// delete, line 134
			slice_del();
			break;
		case 6:
			// (, line 136
			// call R2, line 136
			if (!r_R2()) {
				return false;
			}
			// delete, line 136
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_4() {
		int among_var;
		int v_1;
		// (, line 140
		// [, line 141
		ket = cursor;
		// substring, line 141
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			return false;
		}
		// ], line 141
		bra = cursor;
		// call R2, line 141
		if (!r_R2()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 144
			// delete, line 144
			slice_del();
			break;
		case 2:
			// (, line 145
			// or, line 145
			lab0: do {
				v_1 = limit - cursor;
				lab1: do {
					// literal, line 145
					if (!(eq_s_b("s"))) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_1;
				// literal, line 145
				if (!(eq_s_b("t"))) {
					return false;
				}
			} while (false);
			// delete, line 145
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_5() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 149
		// [, line 150
		ket = cursor;
		// substring, line 150
		among_var = find_among_b(a_8);
		if (among_var == 0) {
			return false;
		}
		// ], line 150
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 151
			// or, line 151
			lab0: do {
				v_1 = limit - cursor;
				lab1: do {
					// call R2, line 151
					if (!r_R2()) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_1;
				// (, line 151
				// call R1, line 151
				if (!r_R1()) {
					return false;
				}
				// not, line 151
				{
					v_2 = limit - cursor;
					lab2: do {
						// call shortv, line 151
						if (!r_shortv()) {
							break lab2;
						}
						return false;
					} while (false);
					cursor = limit - v_2;
				}
			} while (false);
			// delete, line 151
			slice_del();
			break;
		case 2:
			// (, line 152
			// call R2, line 152
			if (!r_R2()) {
				return false;
			}
			// literal, line 152
			if (!(eq_s_b("l"))) {
				return false;
			}
			// delete, line 152
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_exception2() {
		// (, line 156
		// [, line 158
		ket = cursor;
		// substring, line 158
		if (find_among_b(a_9) == 0) {
			return false;
		}
		// ], line 158
		bra = cursor;
		// atlimit, line 158
		if (cursor > limit_backward) {
			return false;
		}
		return true;
	}

	private boolean r_exception1() {
		int among_var;
		// (, line 168
		// [, line 170
		bra = cursor;
		// substring, line 170
		among_var = find_among(a_10);
		if (among_var == 0) {
			return false;
		}
		// ], line 170
		ket = cursor;
		// atlimit, line 170
		if (cursor < limit) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 174
			// <-, line 174
			slice_from("ski");
			break;
		case 2:
			// (, line 175
			// <-, line 175
			slice_from("sky");
			break;
		case 3:
			// (, line 176
			// <-, line 176
			slice_from("die");
			break;
		case 4:
			// (, line 177
			// <-, line 177
			slice_from("lie");
			break;
		case 5:
			// (, line 178
			// <-, line 178
			slice_from("tie");
			break;
		case 6:
			// (, line 182
			// <-, line 182
			slice_from("idl");
			break;
		case 7:
			// (, line 183
			// <-, line 183
			slice_from("gentl");
			break;
		case 8:
			// (, line 184
			// <-, line 184
			slice_from("ugli");
			break;
		case 9:
			// (, line 185
			// <-, line 185
			slice_from("earli");
			break;
		case 10:
			// (, line 186
			// <-, line 186
			slice_from("onli");
			break;
		case 11:
			// (, line 187
			// <-, line 187
			slice_from("singl");
			break;
		}
		return true;
	}

	private boolean r_postlude() {
		int v_1;
		int v_2;
		// (, line 203
		// Boolean test Y_found, line 203
		if (!(B_Y_found)) {
			return false;
		}
		// repeat, line 203
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 203
				// goto, line 203
				golab2: while (true) {
					v_2 = cursor;
					lab3: do {
						// (, line 203
						// [, line 203
						bra = cursor;
						// literal, line 203
						if (!(eq_s("Y"))) {
							break lab3;
						}
						// ], line 203
						ket = cursor;
						cursor = v_2;
						break golab2;
					} while (false);
					cursor = v_2;
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				}
				// <-, line 203
				slice_from("y");
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		// (, line 205
		// or, line 207
		lab0: do {
			v_1 = cursor;
			lab1: do {
				// call exception1, line 207
				if (!r_exception1()) {
					break lab1;
				}
				break lab0;
			} while (false);
			cursor = v_1;
			lab2: do {
				// not, line 208
				{
					v_2 = cursor;
					lab3: do {
						// hop, line 208
						{
							int c = cursor + 3;
							if (0 > c || c > limit) {
								break lab3;
							}
							cursor = c;
						}
						break lab2;
					} while (false);
					cursor = v_2;
				}
				break lab0;
			} while (false);
			cursor = v_1;
			// (, line 208
			// do, line 209
			v_3 = cursor;
			lab4: do {
				// call prelude, line 209
				if (!r_prelude()) {
					break lab4;
				}
			} while (false);
			cursor = v_3;
			// do, line 210
			v_4 = cursor;
			lab5: do {
				// call mark_regions, line 210
				if (!r_mark_regions()) {
					break lab5;
				}
			} while (false);
			cursor = v_4;
			// backwards, line 211
			limit_backward = cursor;
			cursor = limit;
			// (, line 211
			// do, line 213
			v_5 = limit - cursor;
			lab6: do {
				// call Step_1a, line 213
				if (!r_Step_1a()) {
					break lab6;
				}
			} while (false);
			cursor = limit - v_5;
			// or, line 215
			lab7: do {
				v_6 = limit - cursor;
				lab8: do {
					// call exception2, line 215
					if (!r_exception2()) {
						break lab8;
					}
					break lab7;
				} while (false);
				cursor = limit - v_6;
				// (, line 215
				// do, line 217
				v_7 = limit - cursor;
				lab9: do {
					// call Step_1b, line 217
					if (!r_Step_1b()) {
						break lab9;
					}
				} while (false);
				cursor = limit - v_7;
				// do, line 218
				v_8 = limit - cursor;
				lab10: do {
					// call Step_1c, line 218
					if (!r_Step_1c()) {
						break lab10;
					}
				} while (false);
				cursor = limit - v_8;
				// do, line 220
				v_9 = limit - cursor;
				lab11: do {
					// call Step_2, line 220
					if (!r_Step_2()) {
						break lab11;
					}
				} while (false);
				cursor = limit - v_9;
				// do, line 221
				v_10 = limit - cursor;
				lab12: do {
					// call Step_3, line 221
					if (!r_Step_3()) {
						break lab12;
					}
				} while (false);
				cursor = limit - v_10;
				// do, line 222
				v_11 = limit - cursor;
				lab13: do {
					// call Step_4, line 222
					if (!r_Step_4()) {
						break lab13;
					}
				} while (false);
				cursor = limit - v_11;
				// do, line 224
				v_12 = limit - cursor;
				lab14: do {
					// call Step_5, line 224
					if (!r_Step_5()) {
						break lab14;
					}
				} while (false);
				cursor = limit - v_12;
			} while (false);
			cursor = limit_backward; // do, line 227
			v_13 = cursor;
			lab15: do {
				// call postlude, line 227
				if (!r_postlude()) {
					break lab15;
				}
			} while (false);
			cursor = v_13;
		} while (false);
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class finnishStemmer extends SnowballStemmer {

	private final Object es6bridge = this;
	
	private final static Among a_0[] = { new Among("pa", -1, 1), new Among("sti", -1, 2), new Among("kaan", -1, 1),
			new Among("han", -1, 1), new Among("kin", -1, 1), new Among("h\u00E4n", -1, 1),
			new Among("k\u00E4\u00E4n", -1, 1), new Among("ko", -1, 1), new Among("p\u00E4", -1, 1),
			new Among("k\u00F6", -1, 1) };

	private final static Among a_1[] = { new Among("lla", -1, -1), new Among("na", -1, -1), new Among("ssa", -1, -1),
			new Among("ta", -1, -1), new Among("lta", 3, -1), new Among("sta", 3, -1) };

	private final static Among a_2[] = { new Among("ll\u00E4", -1, -1), new Among("n\u00E4", -1, -1),
			new Among("ss\u00E4", -1, -1), new Among("t\u00E4", -1, -1), new Among("lt\u00E4", 3, -1),
			new Among("st\u00E4", 3, -1) };

	private final static Among a_3[] = { new Among("lle", -1, -1), new Among("ine", -1, -1) };

	private final static Among a_4[] = { new Among("nsa", -1, 3), new Among("mme", -1, 3), new Among("nne", -1, 3),
			new Among("ni", -1, 2), new Among("si", -1, 1), new Among("an", -1, 4), new Among("en", -1, 6),
			new Among("\u00E4n", -1, 5), new Among("ns\u00E4", -1, 3) };

	private final static Among a_5[] = { new Among("aa", -1, -1), new Among("ee", -1, -1), new Among("ii", -1, -1),
			new Among("oo", -1, -1), new Among("uu", -1, -1), new Among("\u00E4\u00E4", -1, -1),
			new Among("\u00F6\u00F6", -1, -1) };

	private final Among a_6[] = { new Among("a", -1, 8), new Among("lla", 0, -1), new Among("na", 0, -1),
			new Among("ssa", 0, -1), new Among("ta", 0, -1), new Among("lta", 4, -1), new Among("sta", 4, -1),
			new Among("tta", 4, 9), new Among("lle", -1, -1), new Among("ine", -1, -1), new Among("ksi", -1, -1),
			new Among("n", -1, 7), new Among("han", 11, 1), new Among("den", 11, -1, "r_VI", es6bridge),
			new Among("seen", 11, -1, "r_LONG", es6bridge), new Among("hen", 11, 2),
			new Among("tten", 11, -1, "r_VI", es6bridge), new Among("hin", 11, 3),
			new Among("siin", 11, -1, "r_VI", es6bridge), new Among("hon", 11, 4),
			new Among("h\u00E4n", 11, 5), new Among("h\u00F6n", 11, 6), new Among("\u00E4", -1, 8),
			new Among("ll\u00E4", 22, -1), new Among("n\u00E4", 22, -1), new Among("ss\u00E4", 22, -1),
			new Among("t\u00E4", 22, -1), new Among("lt\u00E4", 26, -1), new Among("st\u00E4", 26, -1),
			new Among("tt\u00E4", 26, 9) };

	private final static Among a_7[] = { new Among("eja", -1, -1), new Among("mma", -1, 1), new Among("imma", 1, -1),
			new Among("mpa", -1, 1), new Among("impa", 3, -1), new Among("mmi", -1, 1), new Among("immi", 5, -1),
			new Among("mpi", -1, 1), new Among("impi", 7, -1), new Among("ej\u00E4", -1, -1),
			new Among("mm\u00E4", -1, 1), new Among("imm\u00E4", 10, -1), new Among("mp\u00E4", -1, 1),
			new Among("imp\u00E4", 12, -1) };

	private final static Among a_8[] = { new Among("i", -1, -1), new Among("j", -1, -1) };

	private final static Among a_9[] = { new Among("mma", -1, 1), new Among("imma", 0, -1) };

	private static final char g_AEI[] = { 17, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8 };

	private static final char g_V1[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32 };

	private static final char g_V2[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32 };

	private static final char g_particle_end[] = { 17, 97, 24, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32 };

	private boolean B_ending_removed;
	private StringBuilder S_x = new StringBuilder();
	private int I_p2;
	private int I_p1;

	private boolean r_mark_regions() {
		int v_1;
		int v_3;
		// (, line 41
		I_p1 = limit;
		I_p2 = limit;
		// goto, line 46
		golab0: while (true) {
			v_1 = cursor;
			lab1: do {
				if (!(in_grouping(g_V1, 97, 246))) {
					break lab1;
				}
				cursor = v_1;
				break golab0;
			} while (false);
			cursor = v_1;
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 46
		golab2: while (true) {
			lab3: do {
				if (!(out_grouping(g_V1, 97, 246))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p1, line 46
		I_p1 = cursor;
		// goto, line 47
		golab4: while (true) {
			v_3 = cursor;
			lab5: do {
				if (!(in_grouping(g_V1, 97, 246))) {
					break lab5;
				}
				cursor = v_3;
				break golab4;
			} while (false);
			cursor = v_3;
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 47
		golab6: while (true) {
			lab7: do {
				if (!(out_grouping(g_V1, 97, 246))) {
					break lab7;
				}
				break golab6;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p2, line 47
		I_p2 = cursor;
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_particle_etc() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 54
		// setlimit, line 55
		v_1 = limit - cursor;
		// tomark, line 55
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 55
		// [, line 55
		ket = cursor;
		// substring, line 55
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 55
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 62
			if (!(in_grouping_b(g_particle_end, 97, 246))) {
				return false;
			}
			break;
		case 2:
			// (, line 64
			// call R2, line 64
			if (!r_R2()) {
				return false;
			}
			break;
		}
		// delete, line 66
		slice_del();
		return true;
	}

	private boolean r_possessive() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		// (, line 68
		// setlimit, line 69
		v_1 = limit - cursor;
		// tomark, line 69
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 69
		// [, line 69
		ket = cursor;
		// substring, line 69
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 69
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
		// (, line 72
		// not, line 72
		{
			v_3 = limit - cursor;
			lab0: do {
				// literal, line 72
				if (!(eq_s_b("k"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = limit - v_3;
		}
			// delete, line 72
			slice_del();
			break;
		case 2:
			// (, line 74
			// delete, line 74
			slice_del();
			// [, line 74
			ket = cursor;
			// literal, line 74
			if (!(eq_s_b("kse"))) {
				return false;
			}
			// ], line 74
			bra = cursor;
			// <-, line 74
			slice_from("ksi");
			break;
		case 3:
			// (, line 78
			// delete, line 78
			slice_del();
			break;
		case 4:
			// (, line 81
			// among, line 81
			if (find_among_b(a_1) == 0) {
				return false;
			}
			// delete, line 81
			slice_del();
			break;
		case 5:
			// (, line 83
			// among, line 83
			if (find_among_b(a_2) == 0) {
				return false;
			}
			// delete, line 84
			slice_del();
			break;
		case 6:
			// (, line 86
			// among, line 86
			if (find_among_b(a_3) == 0) {
				return false;
			}
			// delete, line 86
			slice_del();
			break;
		}
		return true;
	}

	public boolean r_LONG() {
		// among, line 91
		if (find_among_b(a_5) == 0) {
			return false;
		}
		return true;
	}

	public boolean r_VI() {
		// (, line 93
		// literal, line 93
		if (!(eq_s_b("i"))) {
			return false;
		}
		if (!(in_grouping_b(g_V2, 97, 246))) {
			return false;
		}
		return true;
	}

	private boolean r_case_ending() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 95
		// setlimit, line 96
		v_1 = limit - cursor;
		// tomark, line 96
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 96
		// [, line 96
		ket = cursor;
		// substring, line 96
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 96
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 98
			// literal, line 98
			if (!(eq_s_b("a"))) {
				return false;
			}
			break;
		case 2:
			// (, line 99
			// literal, line 99
			if (!(eq_s_b("e"))) {
				return false;
			}
			break;
		case 3:
			// (, line 100
			// literal, line 100
			if (!(eq_s_b("i"))) {
				return false;
			}
			break;
		case 4:
			// (, line 101
			// literal, line 101
			if (!(eq_s_b("o"))) {
				return false;
			}
			break;
		case 5:
			// (, line 102
			// literal, line 102
			if (!(eq_s_b("\u00E4"))) {
				return false;
			}
			break;
		case 6:
			// (, line 103
			// literal, line 103
			if (!(eq_s_b("\u00F6"))) {
				return false;
			}
			break;
		case 7:
			// (, line 111
			// try, line 111
			v_3 = limit - cursor;
			lab0: do {
				// (, line 111
				// and, line 113
				v_4 = limit - cursor;
				// or, line 112
				lab1: do {
					v_5 = limit - cursor;
					lab2: do {
						// call LONG, line 111
						if (!r_LONG()) {
							break lab2;
						}
						break lab1;
					} while (false);
					cursor = limit - v_5;
					// literal, line 112
					if (!(eq_s_b("ie"))) {
						cursor = limit - v_3;
						break lab0;
					}
				} while (false);
				cursor = limit - v_4;
				// next, line 113
				if (cursor <= limit_backward) {
					cursor = limit - v_3;
					break lab0;
				}
				cursor--;
				// ], line 113
				bra = cursor;
			} while (false);
			break;
		case 8:
			// (, line 119
			if (!(in_grouping_b(g_V1, 97, 246))) {
				return false;
			}
			if (!(out_grouping_b(g_V1, 97, 246))) {
				return false;
			}
			break;
		case 9:
			// (, line 121
			// literal, line 121
			if (!(eq_s_b("e"))) {
				return false;
			}
			break;
		}
		// delete, line 138
		slice_del();
		// set ending_removed, line 139
		B_ending_removed = true;
		return true;
	}

	private boolean r_other_endings() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		// (, line 141
		// setlimit, line 142
		v_1 = limit - cursor;
		// tomark, line 142
		if (cursor < I_p2) {
			return false;
		}
		cursor = I_p2;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 142
		// [, line 142
		ket = cursor;
		// substring, line 142
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 142
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
		// (, line 146
		// not, line 146
		{
			v_3 = limit - cursor;
			lab0: do {
				// literal, line 146
				if (!(eq_s_b("po"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = limit - v_3;
		}
			break;
		}
		// delete, line 151
		slice_del();
		return true;
	}

	private boolean r_i_plural() {
		int v_1;
		int v_2;
		// (, line 153
		// setlimit, line 154
		v_1 = limit - cursor;
		// tomark, line 154
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 154
		// [, line 154
		ket = cursor;
		// substring, line 154
		if (find_among_b(a_8) == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 154
		bra = cursor;
		limit_backward = v_2;
		// delete, line 158
		slice_del();
		return true;
	}

	private boolean r_t_plural() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		// (, line 160
		// setlimit, line 161
		v_1 = limit - cursor;
		// tomark, line 161
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 161
		// [, line 162
		ket = cursor;
		// literal, line 162
		if (!(eq_s_b("t"))) {
			limit_backward = v_2;
			return false;
		}
		// ], line 162
		bra = cursor;
		// test, line 162
		v_3 = limit - cursor;
		if (!(in_grouping_b(g_V1, 97, 246))) {
			limit_backward = v_2;
			return false;
		}
		cursor = limit - v_3;
		// delete, line 163
		slice_del();
		limit_backward = v_2;
		// setlimit, line 165
		v_4 = limit - cursor;
		// tomark, line 165
		if (cursor < I_p2) {
			return false;
		}
		cursor = I_p2;
		v_5 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_4;
		// (, line 165
		// [, line 165
		ket = cursor;
		// substring, line 165
		among_var = find_among_b(a_9);
		if (among_var == 0) {
			limit_backward = v_5;
			return false;
		}
		// ], line 165
		bra = cursor;
		limit_backward = v_5;
		switch (among_var) {
		case 0:
			return false;
		case 1:
		// (, line 167
		// not, line 167
		{
			v_6 = limit - cursor;
			lab0: do {
				// literal, line 167
				if (!(eq_s_b("po"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = limit - v_6;
		}
			break;
		}
		// delete, line 170
		slice_del();
		return true;
	}

	private boolean r_tidy() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		// (, line 172
		// setlimit, line 173
		v_1 = limit - cursor;
		// tomark, line 173
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 173
		// do, line 174
		v_3 = limit - cursor;
		lab0: do {
			// (, line 174
			// and, line 174
			v_4 = limit - cursor;
			// call LONG, line 174
			if (!r_LONG()) {
				break lab0;
			}
			cursor = limit - v_4;
			// (, line 174
			// [, line 174
			ket = cursor;
			// next, line 174
			if (cursor <= limit_backward) {
				break lab0;
			}
			cursor--;
			// ], line 174
			bra = cursor;
			// delete, line 174
			slice_del();
		} while (false);
		cursor = limit - v_3;
		// do, line 175
		v_5 = limit - cursor;
		lab1: do {
			// (, line 175
			// [, line 175
			ket = cursor;
			if (!(in_grouping_b(g_AEI, 97, 228))) {
				break lab1;
			}
			// ], line 175
			bra = cursor;
			if (!(out_grouping_b(g_V1, 97, 246))) {
				break lab1;
			}
			// delete, line 175
			slice_del();
		} while (false);
		cursor = limit - v_5;
		// do, line 176
		v_6 = limit - cursor;
		lab2: do {
			// (, line 176
			// [, line 176
			ket = cursor;
			// literal, line 176
			if (!(eq_s_b("j"))) {
				break lab2;
			}
			// ], line 176
			bra = cursor;
			// or, line 176
			lab3: do {
				v_7 = limit - cursor;
				lab4: do {
					// literal, line 176
					if (!(eq_s_b("o"))) {
						break lab4;
					}
					break lab3;
				} while (false);
				cursor = limit - v_7;
				// literal, line 176
				if (!(eq_s_b("u"))) {
					break lab2;
				}
			} while (false);
			// delete, line 176
			slice_del();
		} while (false);
		cursor = limit - v_6;
		// do, line 177
		v_8 = limit - cursor;
		lab5: do {
			// (, line 177
			// [, line 177
			ket = cursor;
			// literal, line 177
			if (!(eq_s_b("o"))) {
				break lab5;
			}
			// ], line 177
			bra = cursor;
			// literal, line 177
			if (!(eq_s_b("j"))) {
				break lab5;
			}
			// delete, line 177
			slice_del();
		} while (false);
		cursor = limit - v_8;
		limit_backward = v_2;
		// goto, line 179
		golab6: while (true) {
			v_9 = limit - cursor;
			lab7: do {
				if (!(out_grouping_b(g_V1, 97, 246))) {
					break lab7;
				}
				cursor = limit - v_9;
				break golab6;
			} while (false);
			cursor = limit - v_9;
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
		}
		// [, line 179
		ket = cursor;
		// next, line 179
		if (cursor <= limit_backward) {
			return false;
		}
		cursor--;
		// ], line 179
		bra = cursor;
		// -> x, line 179
		S_x = slice_to(S_x);
		// name x, line 179
		if (!(eq_s_b(S_x.toString()))) {
			return false;
		}
		// delete, line 179
		slice_del();
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		// (, line 183
		// do, line 185
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 185
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// unset ending_removed, line 186
		B_ending_removed = false;
		// backwards, line 187
		limit_backward = cursor;
		cursor = limit;
		// (, line 187
		// do, line 188
		v_2 = limit - cursor;
		lab1: do {
			// call particle_etc, line 188
			if (!r_particle_etc()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 189
		v_3 = limit - cursor;
		lab2: do {
			// call possessive, line 189
			if (!r_possessive()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 190
		v_4 = limit - cursor;
		lab3: do {
			// call case_ending, line 190
			if (!r_case_ending()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		// do, line 191
		v_5 = limit - cursor;
		lab4: do {
			// call other_endings, line 191
			if (!r_other_endings()) {
				break lab4;
			}
		} while (false);
		cursor = limit - v_5;
		// or, line 192
		lab5: do {
			v_6 = limit - cursor;
			lab6: do {
				// (, line 192
				// Boolean test ending_removed, line 192
				if (!(B_ending_removed)) {
					break lab6;
				}
				// do, line 192
				v_7 = limit - cursor;
				lab7: do {
					// call i_plural, line 192
					if (!r_i_plural()) {
						break lab7;
					}
				} while (false);
				cursor = limit - v_7;
				break lab5;
			} while (false);
			cursor = limit - v_6;
			// do, line 192
			v_8 = limit - cursor;
			lab8: do {
				// call t_plural, line 192
				if (!r_t_plural()) {
					break lab8;
				}
			} while (false);
			cursor = limit - v_8;
		} while (false);
		// do, line 193
		v_9 = limit - cursor;
		lab9: do {
			// call tidy, line 193
			if (!r_tidy()) {
				break lab9;
			}
		} while (false);
		cursor = limit - v_9;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class frenchStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("col", -1, -1), new Among("par", -1, -1), new Among("tap", -1, -1) };

	private final static Among a_1[] = { new Among("", -1, 4), new Among("I", 0, 1), new Among("U", 0, 2),
			new Among("Y", 0, 3) };

	private final static Among a_2[] = { new Among("iqU", -1, 3), new Among("abl", -1, 3), new Among("I\u00E8r", -1, 4),
			new Among("i\u00E8r", -1, 4), new Among("eus", -1, 2), new Among("iv", -1, 1) };

	private final static Among a_3[] = { new Among("ic", -1, 2), new Among("abil", -1, 1), new Among("iv", -1, 3) };

	private final static Among a_4[] = { new Among("iqUe", -1, 1), new Among("atrice", -1, 2), new Among("ance", -1, 1),
			new Among("ence", -1, 5), new Among("logie", -1, 3), new Among("able", -1, 1), new Among("isme", -1, 1),
			new Among("euse", -1, 11), new Among("iste", -1, 1), new Among("ive", -1, 8), new Among("if", -1, 8),
			new Among("usion", -1, 4), new Among("ation", -1, 2), new Among("ution", -1, 4), new Among("ateur", -1, 2),
			new Among("iqUes", -1, 1), new Among("atrices", -1, 2), new Among("ances", -1, 1),
			new Among("ences", -1, 5), new Among("logies", -1, 3), new Among("ables", -1, 1), new Among("ismes", -1, 1),
			new Among("euses", -1, 11), new Among("istes", -1, 1), new Among("ives", -1, 8), new Among("ifs", -1, 8),
			new Among("usions", -1, 4), new Among("ations", -1, 2), new Among("utions", -1, 4),
			new Among("ateurs", -1, 2), new Among("ments", -1, 15), new Among("ements", 30, 6),
			new Among("issements", 31, 12), new Among("it\u00E9s", -1, 7), new Among("ment", -1, 15),
			new Among("ement", 34, 6), new Among("issement", 35, 12), new Among("amment", 34, 13),
			new Among("emment", 34, 14), new Among("aux", -1, 10), new Among("eaux", 39, 9), new Among("eux", -1, 1),
			new Among("it\u00E9", -1, 7) };

	private final static Among a_5[] = { new Among("ira", -1, 1), new Among("ie", -1, 1), new Among("isse", -1, 1),
			new Among("issante", -1, 1), new Among("i", -1, 1), new Among("irai", 4, 1), new Among("ir", -1, 1),
			new Among("iras", -1, 1), new Among("ies", -1, 1), new Among("\u00EEmes", -1, 1), new Among("isses", -1, 1),
			new Among("issantes", -1, 1), new Among("\u00EEtes", -1, 1), new Among("is", -1, 1),
			new Among("irais", 13, 1), new Among("issais", 13, 1), new Among("irions", -1, 1),
			new Among("issions", -1, 1), new Among("irons", -1, 1), new Among("issons", -1, 1),
			new Among("issants", -1, 1), new Among("it", -1, 1), new Among("irait", 21, 1), new Among("issait", 21, 1),
			new Among("issant", -1, 1), new Among("iraIent", -1, 1), new Among("issaIent", -1, 1),
			new Among("irent", -1, 1), new Among("issent", -1, 1), new Among("iront", -1, 1),
			new Among("\u00EEt", -1, 1), new Among("iriez", -1, 1), new Among("issiez", -1, 1),
			new Among("irez", -1, 1), new Among("issez", -1, 1) };

	private final static Among a_6[] = { new Among("a", -1, 3), new Among("era", 0, 2), new Among("asse", -1, 3),
			new Among("ante", -1, 3), new Among("\u00E9e", -1, 2), new Among("ai", -1, 3), new Among("erai", 5, 2),
			new Among("er", -1, 2), new Among("as", -1, 3), new Among("eras", 8, 2), new Among("\u00E2mes", -1, 3),
			new Among("asses", -1, 3), new Among("antes", -1, 3), new Among("\u00E2tes", -1, 3),
			new Among("\u00E9es", -1, 2), new Among("ais", -1, 3), new Among("erais", 15, 2), new Among("ions", -1, 1),
			new Among("erions", 17, 2), new Among("assions", 17, 3), new Among("erons", -1, 2),
			new Among("ants", -1, 3), new Among("\u00E9s", -1, 2), new Among("ait", -1, 3), new Among("erait", 23, 2),
			new Among("ant", -1, 3), new Among("aIent", -1, 3), new Among("eraIent", 26, 2),
			new Among("\u00E8rent", -1, 2), new Among("assent", -1, 3), new Among("eront", -1, 2),
			new Among("\u00E2t", -1, 3), new Among("ez", -1, 2), new Among("iez", 32, 2), new Among("eriez", 33, 2),
			new Among("assiez", 33, 3), new Among("erez", 32, 2), new Among("\u00E9", -1, 2) };

	private final static Among a_7[] = { new Among("e", -1, 3), new Among("I\u00E8re", 0, 2),
			new Among("i\u00E8re", 0, 2), new Among("ion", -1, 1), new Among("Ier", -1, 2), new Among("ier", -1, 2),
			new Among("\u00EB", -1, 4) };

	private final static Among a_8[] = { new Among("ell", -1, -1), new Among("eill", -1, -1), new Among("enn", -1, -1),
			new Among("onn", -1, -1), new Among("ett", -1, -1) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 130, 103, 8, 5 };

	private static final char g_keep_with_s[] = { 1, 65, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };

	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_prelude() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// repeat, line 38
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// goto, line 38
				golab2: while (true) {
					v_2 = cursor;
					lab3: do {
						// (, line 38
						// or, line 44
						lab4: do {
							v_3 = cursor;
							lab5: do {
								// (, line 40
								if (!(in_grouping(g_v, 97, 251))) {
									break lab5;
								}
								// [, line 40
								bra = cursor;
								// or, line 40
								lab6: do {
									v_4 = cursor;
									lab7: do {
										// (, line 40
										// literal, line 40
										if (!(eq_s("u"))) {
											break lab7;
										}
										// ], line 40
										ket = cursor;
										if (!(in_grouping(g_v, 97, 251))) {
											break lab7;
										}
										// <-, line 40
										slice_from("U");
										break lab6;
									} while (false);
									cursor = v_4;
									lab8: do {
										// (, line 41
										// literal, line 41
										if (!(eq_s("i"))) {
											break lab8;
										}
										// ], line 41
										ket = cursor;
										if (!(in_grouping(g_v, 97, 251))) {
											break lab8;
										}
										// <-, line 41
										slice_from("I");
										break lab6;
									} while (false);
									cursor = v_4;
									// (, line 42
									// literal, line 42
									if (!(eq_s("y"))) {
										break lab5;
									}
									// ], line 42
									ket = cursor;
									// <-, line 42
									slice_from("Y");
								} while (false);
								break lab4;
							} while (false);
							cursor = v_3;
							lab9: do {
								// (, line 45
								// [, line 45
								bra = cursor;
								// literal, line 45
								if (!(eq_s("y"))) {
									break lab9;
								}
								// ], line 45
								ket = cursor;
								if (!(in_grouping(g_v, 97, 251))) {
									break lab9;
								}
								// <-, line 45
								slice_from("Y");
								break lab4;
							} while (false);
							cursor = v_3;
							// (, line 47
							// literal, line 47
							if (!(eq_s("q"))) {
								break lab3;
							}
							// [, line 47
							bra = cursor;
							// literal, line 47
							if (!(eq_s("u"))) {
								break lab3;
							}
							// ], line 47
							ket = cursor;
							// <-, line 47
							slice_from("U");
						} while (false);
						cursor = v_2;
						break golab2;
					} while (false);
					cursor = v_2;
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_4;
		// (, line 50
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 56
		v_1 = cursor;
		lab0: do {
			// (, line 56
			// or, line 58
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// (, line 57
					if (!(in_grouping(g_v, 97, 251))) {
						break lab2;
					}
					if (!(in_grouping(g_v, 97, 251))) {
						break lab2;
					}
					// next, line 57
					if (cursor >= limit) {
						break lab2;
					}
					cursor++;
					break lab1;
				} while (false);
				cursor = v_2;
				lab3: do {
					// among, line 59
					if (find_among(a_0) == 0) {
						break lab3;
					}
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 66
				// next, line 66
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
				// gopast, line 66
				golab4: while (true) {
					lab5: do {
						if (!(in_grouping(g_v, 97, 251))) {
							break lab5;
						}
						break golab4;
					} while (false);
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				}
			} while (false);
			// setmark pV, line 67
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 69
		v_4 = cursor;
		lab6: do {
			// (, line 69
			// gopast, line 70
			golab7: while (true) {
				lab8: do {
					if (!(in_grouping(g_v, 97, 251))) {
						break lab8;
					}
					break golab7;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// gopast, line 70
			golab9: while (true) {
				lab10: do {
					if (!(out_grouping(g_v, 97, 251))) {
						break lab10;
					}
					break golab9;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// setmark p1, line 70
			I_p1 = cursor;
			// gopast, line 71
			golab11: while (true) {
				lab12: do {
					if (!(in_grouping(g_v, 97, 251))) {
						break lab12;
					}
					break golab11;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// gopast, line 71
			golab13: while (true) {
				lab14: do {
					if (!(out_grouping(g_v, 97, 251))) {
						break lab14;
					}
					break golab13;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// setmark p2, line 71
			I_p2 = cursor;
		} while (false);
		cursor = v_4;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 75
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 75
				// [, line 77
				bra = cursor;
				// substring, line 77
				among_var = find_among(a_1);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 77
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 78
					// <-, line 78
					slice_from("i");
					break;
				case 2:
					// (, line 79
					// <-, line 79
					slice_from("u");
					break;
				case 3:
					// (, line 80
					// <-, line 80
					slice_from("y");
					break;
				case 4:
					// (, line 81
					// next, line 81
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		// (, line 91
		// [, line 92
		ket = cursor;
		// substring, line 92
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 92
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 96
			// call R2, line 96
			if (!r_R2()) {
				return false;
			}
			// delete, line 96
			slice_del();
			break;
		case 2:
			// (, line 99
			// call R2, line 99
			if (!r_R2()) {
				return false;
			}
			// delete, line 99
			slice_del();
			// try, line 100
			v_1 = limit - cursor;
			lab0: do {
				// (, line 100
				// [, line 100
				ket = cursor;
				// literal, line 100
				if (!(eq_s_b("ic"))) {
					cursor = limit - v_1;
					break lab0;
				}
				// ], line 100
				bra = cursor;
				// or, line 100
				lab1: do {
					v_2 = limit - cursor;
					lab2: do {
						// (, line 100
						// call R2, line 100
						if (!r_R2()) {
							break lab2;
						}
						// delete, line 100
						slice_del();
						break lab1;
					} while (false);
					cursor = limit - v_2;
					// <-, line 100
					slice_from("iqU");
				} while (false);
			} while (false);
			break;
		case 3:
			// (, line 104
			// call R2, line 104
			if (!r_R2()) {
				return false;
			}
			// <-, line 104
			slice_from("log");
			break;
		case 4:
			// (, line 107
			// call R2, line 107
			if (!r_R2()) {
				return false;
			}
			// <-, line 107
			slice_from("u");
			break;
		case 5:
			// (, line 110
			// call R2, line 110
			if (!r_R2()) {
				return false;
			}
			// <-, line 110
			slice_from("ent");
			break;
		case 6:
			// (, line 113
			// call RV, line 114
			if (!r_RV()) {
				return false;
			}
			// delete, line 114
			slice_del();
			// try, line 115
			v_3 = limit - cursor;
			lab3: do {
				// (, line 115
				// [, line 116
				ket = cursor;
				// substring, line 116
				among_var = find_among_b(a_2);
				if (among_var == 0) {
					cursor = limit - v_3;
					break lab3;
				}
				// ], line 116
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_3;
					break lab3;
				case 1:
					// (, line 117
					// call R2, line 117
					if (!r_R2()) {
						cursor = limit - v_3;
						break lab3;
					}
					// delete, line 117
					slice_del();
					// [, line 117
					ket = cursor;
					// literal, line 117
					if (!(eq_s_b("at"))) {
						cursor = limit - v_3;
						break lab3;
					}
					// ], line 117
					bra = cursor;
					// call R2, line 117
					if (!r_R2()) {
						cursor = limit - v_3;
						break lab3;
					}
					// delete, line 117
					slice_del();
					break;
				case 2:
					// (, line 118
					// or, line 118
					lab4: do {
						v_4 = limit - cursor;
						lab5: do {
							// (, line 118
							// call R2, line 118
							if (!r_R2()) {
								break lab5;
							}
							// delete, line 118
							slice_del();
							break lab4;
						} while (false);
						cursor = limit - v_4;
						// (, line 118
						// call R1, line 118
						if (!r_R1()) {
							cursor = limit - v_3;
							break lab3;
						}
						// <-, line 118
						slice_from("eux");
					} while (false);
					break;
				case 3:
					// (, line 120
					// call R2, line 120
					if (!r_R2()) {
						cursor = limit - v_3;
						break lab3;
					}
					// delete, line 120
					slice_del();
					break;
				case 4:
					// (, line 122
					// call RV, line 122
					if (!r_RV()) {
						cursor = limit - v_3;
						break lab3;
					}
					// <-, line 122
					slice_from("i");
					break;
				}
			} while (false);
			break;
		case 7:
			// (, line 128
			// call R2, line 129
			if (!r_R2()) {
				return false;
			}
			// delete, line 129
			slice_del();
			// try, line 130
			v_5 = limit - cursor;
			lab6: do {
				// (, line 130
				// [, line 131
				ket = cursor;
				// substring, line 131
				among_var = find_among_b(a_3);
				if (among_var == 0) {
					cursor = limit - v_5;
					break lab6;
				}
				// ], line 131
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_5;
					break lab6;
				case 1:
					// (, line 132
					// or, line 132
					lab7: do {
						v_6 = limit - cursor;
						lab8: do {
							// (, line 132
							// call R2, line 132
							if (!r_R2()) {
								break lab8;
							}
							// delete, line 132
							slice_del();
							break lab7;
						} while (false);
						cursor = limit - v_6;
						// <-, line 132
						slice_from("abl");
					} while (false);
					break;
				case 2:
					// (, line 133
					// or, line 133
					lab9: do {
						v_7 = limit - cursor;
						lab10: do {
							// (, line 133
							// call R2, line 133
							if (!r_R2()) {
								break lab10;
							}
							// delete, line 133
							slice_del();
							break lab9;
						} while (false);
						cursor = limit - v_7;
						// <-, line 133
						slice_from("iqU");
					} while (false);
					break;
				case 3:
					// (, line 134
					// call R2, line 134
					if (!r_R2()) {
						cursor = limit - v_5;
						break lab6;
					}
					// delete, line 134
					slice_del();
					break;
				}
			} while (false);
			break;
		case 8:
			// (, line 140
			// call R2, line 141
			if (!r_R2()) {
				return false;
			}
			// delete, line 141
			slice_del();
			// try, line 142
			v_8 = limit - cursor;
			lab11: do {
				// (, line 142
				// [, line 142
				ket = cursor;
				// literal, line 142
				if (!(eq_s_b("at"))) {
					cursor = limit - v_8;
					break lab11;
				}
				// ], line 142
				bra = cursor;
				// call R2, line 142
				if (!r_R2()) {
					cursor = limit - v_8;
					break lab11;
				}
				// delete, line 142
				slice_del();
				// [, line 142
				ket = cursor;
				// literal, line 142
				if (!(eq_s_b("ic"))) {
					cursor = limit - v_8;
					break lab11;
				}
				// ], line 142
				bra = cursor;
				// or, line 142
				lab12: do {
					v_9 = limit - cursor;
					lab13: do {
						// (, line 142
						// call R2, line 142
						if (!r_R2()) {
							break lab13;
						}
						// delete, line 142
						slice_del();
						break lab12;
					} while (false);
					cursor = limit - v_9;
					// <-, line 142
					slice_from("iqU");
				} while (false);
			} while (false);
			break;
		case 9:
			// (, line 144
			// <-, line 144
			slice_from("eau");
			break;
		case 10:
			// (, line 145
			// call R1, line 145
			if (!r_R1()) {
				return false;
			}
			// <-, line 145
			slice_from("al");
			break;
		case 11:
			// (, line 147
			// or, line 147
			lab14: do {
				v_10 = limit - cursor;
				lab15: do {
					// (, line 147
					// call R2, line 147
					if (!r_R2()) {
						break lab15;
					}
					// delete, line 147
					slice_del();
					break lab14;
				} while (false);
				cursor = limit - v_10;
				// (, line 147
				// call R1, line 147
				if (!r_R1()) {
					return false;
				}
				// <-, line 147
				slice_from("eux");
			} while (false);
			break;
		case 12:
			// (, line 150
			// call R1, line 150
			if (!r_R1()) {
				return false;
			}
			if (!(out_grouping_b(g_v, 97, 251))) {
				return false;
			}
			// delete, line 150
			slice_del();
			break;
		case 13:
			// (, line 155
			// call RV, line 155
			if (!r_RV()) {
				return false;
			}
			// fail, line 155
			// (, line 155
			// <-, line 155
			slice_from("ant");
			return false;
		case 14:
			// (, line 156
			// call RV, line 156
			if (!r_RV()) {
				return false;
			}
			// fail, line 156
			// (, line 156
			// <-, line 156
			slice_from("ent");
			return false;
		case 15:
			// (, line 158
			// test, line 158
			v_11 = limit - cursor;
			// (, line 158
			if (!(in_grouping_b(g_v, 97, 251))) {
				return false;
			}
			// call RV, line 158
			if (!r_RV()) {
				return false;
			}
			cursor = limit - v_11;
			// fail, line 158
			// (, line 158
			// delete, line 158
			slice_del();
			return false;
		}
		return true;
	}

	private boolean r_i_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// setlimit, line 163
		v_1 = limit - cursor;
		// tomark, line 163
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 163
		// [, line 164
		ket = cursor;
		// substring, line 164
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 164
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_2;
			return false;
		case 1:
			// (, line 170
			if (!(out_grouping_b(g_v, 97, 251))) {
				limit_backward = v_2;
				return false;
			}
			// delete, line 170
			slice_del();
			break;
		}
		limit_backward = v_2;
		return true;
	}

	private boolean r_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		// setlimit, line 174
		v_1 = limit - cursor;
		// tomark, line 174
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 174
		// [, line 175
		ket = cursor;
		// substring, line 175
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 175
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_2;
			return false;
		case 1:
			// (, line 177
			// call R2, line 177
			if (!r_R2()) {
				limit_backward = v_2;
				return false;
			}
			// delete, line 177
			slice_del();
			break;
		case 2:
			// (, line 185
			// delete, line 185
			slice_del();
			break;
		case 3:
			// (, line 190
			// delete, line 190
			slice_del();
			// try, line 191
			v_3 = limit - cursor;
			lab0: do {
				// (, line 191
				// [, line 191
				ket = cursor;
				// literal, line 191
				if (!(eq_s_b("e"))) {
					cursor = limit - v_3;
					break lab0;
				}
				// ], line 191
				bra = cursor;
				// delete, line 191
				slice_del();
			} while (false);
			break;
		}
		limit_backward = v_2;
		return true;
	}

	private boolean r_residual_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 198
		// try, line 199
		v_1 = limit - cursor;
		lab0: do {
			// (, line 199
			// [, line 199
			ket = cursor;
			// literal, line 199
			if (!(eq_s_b("s"))) {
				cursor = limit - v_1;
				break lab0;
			}
			// ], line 199
			bra = cursor;
			// test, line 199
			v_2 = limit - cursor;
			if (!(out_grouping_b(g_keep_with_s, 97, 232))) {
				cursor = limit - v_1;
				break lab0;
			}
			cursor = limit - v_2;
			// delete, line 199
			slice_del();
		} while (false);
		// setlimit, line 200
		v_3 = limit - cursor;
		// tomark, line 200
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_4 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_3;
		// (, line 200
		// [, line 201
		ket = cursor;
		// substring, line 201
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			limit_backward = v_4;
			return false;
		}
		// ], line 201
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_4;
			return false;
		case 1:
			// (, line 202
			// call R2, line 202
			if (!r_R2()) {
				limit_backward = v_4;
				return false;
			}
			// or, line 202
			lab1: do {
				v_5 = limit - cursor;
				lab2: do {
					// literal, line 202
					if (!(eq_s_b("s"))) {
						break lab2;
					}
					break lab1;
				} while (false);
				cursor = limit - v_5;
				// literal, line 202
				if (!(eq_s_b("t"))) {
					limit_backward = v_4;
					return false;
				}
			} while (false);
			// delete, line 202
			slice_del();
			break;
		case 2:
			// (, line 204
			// <-, line 204
			slice_from("i");
			break;
		case 3:
			// (, line 205
			// delete, line 205
			slice_del();
			break;
		case 4:
			// (, line 206
			// literal, line 206
			if (!(eq_s_b("gu"))) {
				limit_backward = v_4;
				return false;
			}
			// delete, line 206
			slice_del();
			break;
		}
		limit_backward = v_4;
		return true;
	}

	private boolean r_un_double() {
		int v_1;
		// (, line 211
		// test, line 212
		v_1 = limit - cursor;
		// among, line 212
		if (find_among_b(a_8) == 0) {
			return false;
		}
		cursor = limit - v_1;
		// [, line 212
		ket = cursor;
		// next, line 212
		if (cursor <= limit_backward) {
			return false;
		}
		cursor--;
		// ], line 212
		bra = cursor;
		// delete, line 212
		slice_del();
		return true;
	}

	private boolean r_un_accent() {
		int v_3;
		// (, line 215
		// atleast, line 216
		{
			int v_1 = 1;
			// atleast, line 216
			replab0: while (true) {
				lab1: do {
					if (!(out_grouping_b(g_v, 97, 251))) {
						break lab1;
					}
					v_1--;
					continue replab0;
				} while (false);
				break replab0;
			}
			if (v_1 > 0) {
				return false;
			}
		}
		// [, line 217
		ket = cursor;
		// or, line 217
		lab2: do {
			v_3 = limit - cursor;
			lab3: do {
				// literal, line 217
				if (!(eq_s_b("\u00E9"))) {
					break lab3;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			// literal, line 217
			if (!(eq_s_b("\u00E8"))) {
				return false;
			}
		} while (false);
		// ], line 217
		bra = cursor;
		// <-, line 217
		slice_from("e");
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		// (, line 221
		// do, line 223
		v_1 = cursor;
		lab0: do {
			// call prelude, line 223
			if (!r_prelude()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 224
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 224
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 225
		limit_backward = cursor;
		cursor = limit;
		// (, line 225
		// do, line 227
		v_3 = limit - cursor;
		lab2: do {
			// (, line 227
			// or, line 237
			lab3: do {
				v_4 = limit - cursor;
				lab4: do {
					// (, line 228
					// and, line 233
					v_5 = limit - cursor;
					// (, line 229
					// or, line 229
					lab5: do {
						v_6 = limit - cursor;
						lab6: do {
							// call standard_suffix, line 229
							if (!r_standard_suffix()) {
								break lab6;
							}
							break lab5;
						} while (false);
						cursor = limit - v_6;
						lab7: do {
							// call i_verb_suffix, line 230
							if (!r_i_verb_suffix()) {
								break lab7;
							}
							break lab5;
						} while (false);
						cursor = limit - v_6;
						// call verb_suffix, line 231
						if (!r_verb_suffix()) {
							break lab4;
						}
					} while (false);
					cursor = limit - v_5;
					// try, line 234
					v_7 = limit - cursor;
					lab8: do {
						// (, line 234
						// [, line 234
						ket = cursor;
						// or, line 234
						lab9: do {
							v_8 = limit - cursor;
							lab10: do {
								// (, line 234
								// literal, line 234
								if (!(eq_s_b("Y"))) {
									break lab10;
								}
								// ], line 234
								bra = cursor;
								// <-, line 234
								slice_from("i");
								break lab9;
							} while (false);
							cursor = limit - v_8;
							// (, line 235
							// literal, line 235
							if (!(eq_s_b("\u00E7"))) {
								cursor = limit - v_7;
								break lab8;
							}
							// ], line 235
							bra = cursor;
							// <-, line 235
							slice_from("c");
						} while (false);
					} while (false);
					break lab3;
				} while (false);
				cursor = limit - v_4;
				// call residual_suffix, line 238
				if (!r_residual_suffix()) {
					break lab2;
				}
			} while (false);
		} while (false);
		cursor = limit - v_3;
		// do, line 243
		v_9 = limit - cursor;
		lab11: do {
			// call un_double, line 243
			if (!r_un_double()) {
				break lab11;
			}
		} while (false);
		cursor = limit - v_9;
		// do, line 244
		v_10 = limit - cursor;
		lab12: do {
			// call un_accent, line 244
			if (!r_un_accent()) {
				break lab12;
			}
		} while (false);
		cursor = limit - v_10;
		cursor = limit_backward; // do, line 246
		v_11 = cursor;
		lab13: do {
			// call postlude, line 246
			if (!r_postlude()) {
				break lab13;
			}
		} while (false);
		cursor = v_11;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class germanStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 6), new Among("U", 0, 2), new Among("Y", 0, 1),
			new Among("\u00E4", 0, 3), new Among("\u00F6", 0, 4), new Among("\u00FC", 0, 5) };

	private final static Among a_1[] = { new Among("e", -1, 2), new Among("em", -1, 1), new Among("en", -1, 2),
			new Among("ern", -1, 1), new Among("er", -1, 1), new Among("s", -1, 3), new Among("es", 5, 2) };

	private final static Among a_2[] = { new Among("en", -1, 1), new Among("er", -1, 1), new Among("st", -1, 2),
			new Among("est", 2, 1) };

	private final static Among a_3[] = { new Among("ig", -1, 1), new Among("lich", -1, 1) };

	private final static Among a_4[] = { new Among("end", -1, 1), new Among("ig", -1, 2), new Among("ung", -1, 1),
			new Among("lich", -1, 3), new Among("isch", -1, 2), new Among("ik", -1, 2), new Among("heit", -1, 3),
			new Among("keit", -1, 4) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 32, 8 };

	private static final char g_s_ending[] = { 117, 30, 5 };

	private static final char g_st_ending[] = { 117, 30, 4 };

	private int I_x;
	private int I_p2;
	private int I_p1;

	private boolean r_prelude() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		// (, line 33
		// test, line 35
		v_1 = cursor;
		// repeat, line 35
		replab0: while (true) {
			v_2 = cursor;
			lab1: do {
				// (, line 35
				// or, line 38
				lab2: do {
					v_3 = cursor;
					lab3: do {
						// (, line 36
						// [, line 37
						bra = cursor;
						// literal, line 37
						if (!(eq_s("\u00DF"))) {
							break lab3;
						}
						// ], line 37
						ket = cursor;
						// <-, line 37
						slice_from("ss");
						break lab2;
					} while (false);
					cursor = v_3;
					// next, line 38
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				} while (false);
				continue replab0;
			} while (false);
			cursor = v_2;
			break replab0;
		}
		cursor = v_1;
		// repeat, line 41
		replab4: while (true) {
			v_4 = cursor;
			lab5: do {
				// goto, line 41
				golab6: while (true) {
					v_5 = cursor;
					lab7: do {
						// (, line 41
						if (!(in_grouping(g_v, 97, 252))) {
							break lab7;
						}
						// [, line 42
						bra = cursor;
						// or, line 42
						lab8: do {
							v_6 = cursor;
							lab9: do {
								// (, line 42
								// literal, line 42
								if (!(eq_s("u"))) {
									break lab9;
								}
								// ], line 42
								ket = cursor;
								if (!(in_grouping(g_v, 97, 252))) {
									break lab9;
								}
								// <-, line 42
								slice_from("U");
								break lab8;
							} while (false);
							cursor = v_6;
							// (, line 43
							// literal, line 43
							if (!(eq_s("y"))) {
								break lab7;
							}
							// ], line 43
							ket = cursor;
							if (!(in_grouping(g_v, 97, 252))) {
								break lab7;
							}
							// <-, line 43
							slice_from("Y");
						} while (false);
						cursor = v_5;
						break golab6;
					} while (false);
					cursor = v_5;
					if (cursor >= limit) {
						break lab5;
					}
					cursor++;
				}
				continue replab4;
			} while (false);
			cursor = v_4;
			break replab4;
		}
		return true;
	}

	private boolean r_mark_regions() {
		int v_1;
		// (, line 47
		I_p1 = limit;
		I_p2 = limit;
		// test, line 52
		v_1 = cursor;
		// (, line 52
		// hop, line 52
		{
			int c = cursor + 3;
			if (0 > c || c > limit) {
				return false;
			}
			cursor = c;
		}
		// setmark x, line 52
		I_x = cursor;
		cursor = v_1;
		// gopast, line 54
		golab0: while (true) {
			lab1: do {
				if (!(in_grouping(g_v, 97, 252))) {
					break lab1;
				}
				break golab0;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 54
		golab2: while (true) {
			lab3: do {
				if (!(out_grouping(g_v, 97, 252))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p1, line 54
		I_p1 = cursor;
		// try, line 55
		lab4: do {
			// (, line 55
			if (!(I_p1 < I_x)) {
				break lab4;
			}
			I_p1 = I_x;
		} while (false);
		// gopast, line 56
		golab5: while (true) {
			lab6: do {
				if (!(in_grouping(g_v, 97, 252))) {
					break lab6;
				}
				break golab5;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 56
		golab7: while (true) {
			lab8: do {
				if (!(out_grouping(g_v, 97, 252))) {
					break lab8;
				}
				break golab7;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p2, line 56
		I_p2 = cursor;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 60
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 60
				// [, line 62
				bra = cursor;
				// substring, line 62
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 62
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 63
					// <-, line 63
					slice_from("y");
					break;
				case 2:
					// (, line 64
					// <-, line 64
					slice_from("u");
					break;
				case 3:
					// (, line 65
					// <-, line 65
					slice_from("a");
					break;
				case 4:
					// (, line 66
					// <-, line 66
					slice_from("o");
					break;
				case 5:
					// (, line 67
					// <-, line 67
					slice_from("u");
					break;
				case 6:
					// (, line 68
					// next, line 68
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 78
		// do, line 79
		v_1 = limit - cursor;
		lab0: do {
			// (, line 79
			// [, line 80
			ket = cursor;
			// substring, line 80
			among_var = find_among_b(a_1);
			if (among_var == 0) {
				break lab0;
			}
			// ], line 80
			bra = cursor;
			// call R1, line 80
			if (!r_R1()) {
				break lab0;
			}
			switch (among_var) {
			case 0:
				break lab0;
			case 1:
				// (, line 82
				// delete, line 82
				slice_del();
				break;
			case 2:
				// (, line 85
				// delete, line 85
				slice_del();
				// try, line 86
				v_2 = limit - cursor;
				lab1: do {
					// (, line 86
					// [, line 86
					ket = cursor;
					// literal, line 86
					if (!(eq_s_b("s"))) {
						cursor = limit - v_2;
						break lab1;
					}
					// ], line 86
					bra = cursor;
					// literal, line 86
					if (!(eq_s_b("nis"))) {
						cursor = limit - v_2;
						break lab1;
					}
					// delete, line 86
					slice_del();
				} while (false);
				break;
			case 3:
				// (, line 89
				if (!(in_grouping_b(g_s_ending, 98, 116))) {
					break lab0;
				}
				// delete, line 89
				slice_del();
				break;
			}
		} while (false);
		cursor = limit - v_1;
		// do, line 93
		v_3 = limit - cursor;
		lab2: do {
			// (, line 93
			// [, line 94
			ket = cursor;
			// substring, line 94
			among_var = find_among_b(a_2);
			if (among_var == 0) {
				break lab2;
			}
			// ], line 94
			bra = cursor;
			// call R1, line 94
			if (!r_R1()) {
				break lab2;
			}
			switch (among_var) {
			case 0:
				break lab2;
			case 1:
				// (, line 96
				// delete, line 96
				slice_del();
				break;
			case 2:
				// (, line 99
				if (!(in_grouping_b(g_st_ending, 98, 116))) {
					break lab2;
				}
			// hop, line 99
			{
				int c = cursor - 3;
				if (limit_backward > c || c > limit) {
					break lab2;
				}
				cursor = c;
			}
				// delete, line 99
				slice_del();
				break;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 103
		v_4 = limit - cursor;
		lab3: do {
			// (, line 103
			// [, line 104
			ket = cursor;
			// substring, line 104
			among_var = find_among_b(a_4);
			if (among_var == 0) {
				break lab3;
			}
			// ], line 104
			bra = cursor;
			// call R2, line 104
			if (!r_R2()) {
				break lab3;
			}
			switch (among_var) {
			case 0:
				break lab3;
			case 1:
				// (, line 106
				// delete, line 106
				slice_del();
				// try, line 107
				v_5 = limit - cursor;
				lab4: do {
					// (, line 107
					// [, line 107
					ket = cursor;
					// literal, line 107
					if (!(eq_s_b("ig"))) {
						cursor = limit - v_5;
						break lab4;
					}
					// ], line 107
					bra = cursor;
					// not, line 107
					{
						v_6 = limit - cursor;
						lab5: do {
							// literal, line 107
							if (!(eq_s_b("e"))) {
								break lab5;
							}
							cursor = limit - v_5;
							break lab4;
						} while (false);
						cursor = limit - v_6;
					}
					// call R2, line 107
					if (!r_R2()) {
						cursor = limit - v_5;
						break lab4;
					}
					// delete, line 107
					slice_del();
				} while (false);
				break;
			case 2:
			// (, line 110
			// not, line 110
			{
				v_7 = limit - cursor;
				lab6: do {
					// literal, line 110
					if (!(eq_s_b("e"))) {
						break lab6;
					}
					break lab3;
				} while (false);
				cursor = limit - v_7;
			}
				// delete, line 110
				slice_del();
				break;
			case 3:
				// (, line 113
				// delete, line 113
				slice_del();
				// try, line 114
				v_8 = limit - cursor;
				lab7: do {
					// (, line 114
					// [, line 115
					ket = cursor;
					// or, line 115
					lab8: do {
						v_9 = limit - cursor;
						lab9: do {
							// literal, line 115
							if (!(eq_s_b("er"))) {
								break lab9;
							}
							break lab8;
						} while (false);
						cursor = limit - v_9;
						// literal, line 115
						if (!(eq_s_b("en"))) {
							cursor = limit - v_8;
							break lab7;
						}
					} while (false);
					// ], line 115
					bra = cursor;
					// call R1, line 115
					if (!r_R1()) {
						cursor = limit - v_8;
						break lab7;
					}
					// delete, line 115
					slice_del();
				} while (false);
				break;
			case 4:
				// (, line 119
				// delete, line 119
				slice_del();
				// try, line 120
				v_10 = limit - cursor;
				lab10: do {
					// (, line 120
					// [, line 121
					ket = cursor;
					// substring, line 121
					among_var = find_among_b(a_3);
					if (among_var == 0) {
						cursor = limit - v_10;
						break lab10;
					}
					// ], line 121
					bra = cursor;
					// call R2, line 121
					if (!r_R2()) {
						cursor = limit - v_10;
						break lab10;
					}
					switch (among_var) {
					case 0:
						cursor = limit - v_10;
						break lab10;
					case 1:
						// (, line 123
						// delete, line 123
						slice_del();
						break;
					}
				} while (false);
				break;
			}
		} while (false);
		cursor = limit - v_4;
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 133
		// do, line 134
		v_1 = cursor;
		lab0: do {
			// call prelude, line 134
			if (!r_prelude()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 135
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 135
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 136
		limit_backward = cursor;
		cursor = limit;
		// do, line 137
		v_3 = limit - cursor;
		lab2: do {
			// call standard_suffix, line 137
			if (!r_standard_suffix()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		cursor = limit_backward; // do, line 138
		v_4 = cursor;
		lab3: do {
			// call postlude, line 138
			if (!r_postlude()) {
				break lab3;
			}
		} while (false);
		cursor = v_4;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class hungarianStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("cs", -1, -1), new Among("dzs", -1, -1), new Among("gy", -1, -1),
			new Among("ly", -1, -1), new Among("ny", -1, -1), new Among("sz", -1, -1), new Among("ty", -1, -1),
			new Among("zs", -1, -1) };

	private final static Among a_1[] = { new Among("\u00E1", -1, 1), new Among("\u00E9", -1, 2) };

	private final static Among a_2[] = { new Among("bb", -1, -1), new Among("cc", -1, -1), new Among("dd", -1, -1),
			new Among("ff", -1, -1), new Among("gg", -1, -1), new Among("jj", -1, -1), new Among("kk", -1, -1),
			new Among("ll", -1, -1), new Among("mm", -1, -1), new Among("nn", -1, -1), new Among("pp", -1, -1),
			new Among("rr", -1, -1), new Among("ccs", -1, -1), new Among("ss", -1, -1), new Among("zzs", -1, -1),
			new Among("tt", -1, -1), new Among("vv", -1, -1), new Among("ggy", -1, -1), new Among("lly", -1, -1),
			new Among("nny", -1, -1), new Among("tty", -1, -1), new Among("ssz", -1, -1), new Among("zz", -1, -1) };

	private final static Among a_3[] = { new Among("al", -1, 1), new Among("el", -1, 2) };

	private final static Among a_4[] = { new Among("ba", -1, -1), new Among("ra", -1, -1), new Among("be", -1, -1),
			new Among("re", -1, -1), new Among("ig", -1, -1), new Among("nak", -1, -1), new Among("nek", -1, -1),
			new Among("val", -1, -1), new Among("vel", -1, -1), new Among("ul", -1, -1), new Among("n\u00E1l", -1, -1),
			new Among("n\u00E9l", -1, -1), new Among("b\u00F3l", -1, -1), new Among("r\u00F3l", -1, -1),
			new Among("t\u00F3l", -1, -1), new Among("\u00FCl", -1, -1), new Among("b\u0151l", -1, -1),
			new Among("r\u0151l", -1, -1), new Among("t\u0151l", -1, -1), new Among("n", -1, -1),
			new Among("an", 19, -1), new Among("ban", 20, -1), new Among("en", 19, -1), new Among("ben", 22, -1),
			new Among("k\u00E9ppen", 22, -1), new Among("on", 19, -1), new Among("\u00F6n", 19, -1),
			new Among("k\u00E9pp", -1, -1), new Among("kor", -1, -1), new Among("t", -1, -1), new Among("at", 29, -1),
			new Among("et", 29, -1), new Among("k\u00E9nt", 29, -1), new Among("ank\u00E9nt", 32, -1),
			new Among("enk\u00E9nt", 32, -1), new Among("onk\u00E9nt", 32, -1), new Among("ot", 29, -1),
			new Among("\u00E9rt", 29, -1), new Among("\u00F6t", 29, -1), new Among("hez", -1, -1),
			new Among("hoz", -1, -1), new Among("h\u00F6z", -1, -1), new Among("v\u00E1", -1, -1),
			new Among("v\u00E9", -1, -1) };

	private final static Among a_5[] = { new Among("\u00E1n", -1, 2), new Among("\u00E9n", -1, 1),
			new Among("\u00E1nk\u00E9nt", -1, 3) };

	private final static Among a_6[] = { new Among("stul", -1, 2), new Among("astul", 0, 1),
			new Among("\u00E1stul", 0, 3), new Among("st\u00FCl", -1, 2), new Among("est\u00FCl", 3, 1),
			new Among("\u00E9st\u00FCl", 3, 4) };

	private final static Among a_7[] = { new Among("\u00E1", -1, 1), new Among("\u00E9", -1, 2) };

	private final static Among a_8[] = { new Among("k", -1, 7), new Among("ak", 0, 4), new Among("ek", 0, 6),
			new Among("ok", 0, 5), new Among("\u00E1k", 0, 1), new Among("\u00E9k", 0, 2), new Among("\u00F6k", 0, 3) };

	private final static Among a_9[] = { new Among("\u00E9i", -1, 7), new Among("\u00E1\u00E9i", 0, 6),
			new Among("\u00E9\u00E9i", 0, 5), new Among("\u00E9", -1, 9), new Among("k\u00E9", 3, 4),
			new Among("ak\u00E9", 4, 1), new Among("ek\u00E9", 4, 1), new Among("ok\u00E9", 4, 1),
			new Among("\u00E1k\u00E9", 4, 3), new Among("\u00E9k\u00E9", 4, 2), new Among("\u00F6k\u00E9", 4, 1),
			new Among("\u00E9\u00E9", 3, 8) };

	private final static Among a_10[] = { new Among("a", -1, 18), new Among("ja", 0, 17), new Among("d", -1, 16),
			new Among("ad", 2, 13), new Among("ed", 2, 13), new Among("od", 2, 13), new Among("\u00E1d", 2, 14),
			new Among("\u00E9d", 2, 15), new Among("\u00F6d", 2, 13), new Among("e", -1, 18), new Among("je", 9, 17),
			new Among("nk", -1, 4), new Among("unk", 11, 1), new Among("\u00E1nk", 11, 2), new Among("\u00E9nk", 11, 3),
			new Among("\u00FCnk", 11, 1), new Among("uk", -1, 8), new Among("juk", 16, 7),
			new Among("\u00E1juk", 17, 5), new Among("\u00FCk", -1, 8), new Among("j\u00FCk", 19, 7),
			new Among("\u00E9j\u00FCk", 20, 6), new Among("m", -1, 12), new Among("am", 22, 9), new Among("em", 22, 9),
			new Among("om", 22, 9), new Among("\u00E1m", 22, 10), new Among("\u00E9m", 22, 11), new Among("o", -1, 18),
			new Among("\u00E1", -1, 19), new Among("\u00E9", -1, 20) };

	private final static Among a_11[] = { new Among("id", -1, 10), new Among("aid", 0, 9), new Among("jaid", 1, 6),
			new Among("eid", 0, 9), new Among("jeid", 3, 6), new Among("\u00E1id", 0, 7), new Among("\u00E9id", 0, 8),
			new Among("i", -1, 15), new Among("ai", 7, 14), new Among("jai", 8, 11), new Among("ei", 7, 14),
			new Among("jei", 10, 11), new Among("\u00E1i", 7, 12), new Among("\u00E9i", 7, 13),
			new Among("itek", -1, 24), new Among("eitek", 14, 21), new Among("jeitek", 15, 20),
			new Among("\u00E9itek", 14, 23), new Among("ik", -1, 29), new Among("aik", 18, 26),
			new Among("jaik", 19, 25), new Among("eik", 18, 26), new Among("jeik", 21, 25),
			new Among("\u00E1ik", 18, 27), new Among("\u00E9ik", 18, 28), new Among("ink", -1, 20),
			new Among("aink", 25, 17), new Among("jaink", 26, 16), new Among("eink", 25, 17),
			new Among("jeink", 28, 16), new Among("\u00E1ink", 25, 18), new Among("\u00E9ink", 25, 19),
			new Among("aitok", -1, 21), new Among("jaitok", 32, 20), new Among("\u00E1itok", -1, 22),
			new Among("im", -1, 5), new Among("aim", 35, 4), new Among("jaim", 36, 1), new Among("eim", 35, 4),
			new Among("jeim", 38, 1), new Among("\u00E1im", 35, 2), new Among("\u00E9im", 35, 3) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 36, 10, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 1, 0, 0, 0, 1 };

	private int I_p1;

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_3;
		// (, line 44
		I_p1 = limit;
		// or, line 51
		lab0: do {
			v_1 = cursor;
			lab1: do {
				// (, line 48
				if (!(in_grouping(g_v, 97, 369))) {
					break lab1;
				}
				// goto, line 48
				golab2: while (true) {
					v_2 = cursor;
					lab3: do {
						if (!(out_grouping(g_v, 97, 369))) {
							break lab3;
						}
						cursor = v_2;
						break golab2;
					} while (false);
					cursor = v_2;
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				}
				// or, line 49
				lab4: do {
					v_3 = cursor;
					lab5: do {
						// among, line 49
						if (find_among(a_0) == 0) {
							break lab5;
						}
						break lab4;
					} while (false);
					cursor = v_3;
					// next, line 49
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				} while (false);
				// setmark p1, line 50
				I_p1 = cursor;
				break lab0;
			} while (false);
			cursor = v_1;
			// (, line 53
			if (!(out_grouping(g_v, 97, 369))) {
				return false;
			}
			// gopast, line 53
			golab6: while (true) {
				lab7: do {
					if (!(in_grouping(g_v, 97, 369))) {
						break lab7;
					}
					break golab6;
				} while (false);
				if (cursor >= limit) {
					return false;
				}
				cursor++;
			}
			// setmark p1, line 53
			I_p1 = cursor;
		} while (false);
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_v_ending() {
		int among_var;
		// (, line 60
		// [, line 61
		ket = cursor;
		// substring, line 61
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 61
		bra = cursor;
		// call R1, line 61
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 62
			// <-, line 62
			slice_from("a");
			break;
		case 2:
			// (, line 63
			// <-, line 63
			slice_from("e");
			break;
		}
		return true;
	}

	private boolean r_double() {
		int v_1;
		// (, line 67
		// test, line 68
		v_1 = limit - cursor;
		// among, line 68
		if (find_among_b(a_2) == 0) {
			return false;
		}
		cursor = limit - v_1;
		return true;
	}

	private boolean r_undouble() {
		// (, line 72
		// next, line 73
		if (cursor <= limit_backward) {
			return false;
		}
		cursor--;
		// [, line 73
		ket = cursor;
		// hop, line 73
		{
			int c = cursor - 1;
			if (limit_backward > c || c > limit) {
				return false;
			}
			cursor = c;
		}
		// ], line 73
		bra = cursor;
		// delete, line 73
		slice_del();
		return true;
	}

	private boolean r_instrum() {
		int among_var;
		// (, line 76
		// [, line 77
		ket = cursor;
		// substring, line 77
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 77
		bra = cursor;
		// call R1, line 77
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 78
			// call double, line 78
			if (!r_double()) {
				return false;
			}
			break;
		case 2:
			// (, line 79
			// call double, line 79
			if (!r_double()) {
				return false;
			}
			break;
		}
		// delete, line 81
		slice_del();
		// call undouble, line 82
		if (!r_undouble()) {
			return false;
		}
		return true;
	}

	private boolean r_case() {
		// (, line 86
		// [, line 87
		ket = cursor;
		// substring, line 87
		if (find_among_b(a_4) == 0) {
			return false;
		}
		// ], line 87
		bra = cursor;
		// call R1, line 87
		if (!r_R1()) {
			return false;
		}
		// delete, line 111
		slice_del();
		// call v_ending, line 112
		if (!r_v_ending()) {
			return false;
		}
		return true;
	}

	private boolean r_case_special() {
		int among_var;
		// (, line 115
		// [, line 116
		ket = cursor;
		// substring, line 116
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 116
		bra = cursor;
		// call R1, line 116
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 117
			// <-, line 117
			slice_from("e");
			break;
		case 2:
			// (, line 118
			// <-, line 118
			slice_from("a");
			break;
		case 3:
			// (, line 119
			// <-, line 119
			slice_from("a");
			break;
		}
		return true;
	}

	private boolean r_case_other() {
		int among_var;
		// (, line 123
		// [, line 124
		ket = cursor;
		// substring, line 124
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 124
		bra = cursor;
		// call R1, line 124
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 125
			// delete, line 125
			slice_del();
			break;
		case 2:
			// (, line 126
			// delete, line 126
			slice_del();
			break;
		case 3:
			// (, line 127
			// <-, line 127
			slice_from("a");
			break;
		case 4:
			// (, line 128
			// <-, line 128
			slice_from("e");
			break;
		}
		return true;
	}

	private boolean r_factive() {
		int among_var;
		// (, line 132
		// [, line 133
		ket = cursor;
		// substring, line 133
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			return false;
		}
		// ], line 133
		bra = cursor;
		// call R1, line 133
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 134
			// call double, line 134
			if (!r_double()) {
				return false;
			}
			break;
		case 2:
			// (, line 135
			// call double, line 135
			if (!r_double()) {
				return false;
			}
			break;
		}
		// delete, line 137
		slice_del();
		// call undouble, line 138
		if (!r_undouble()) {
			return false;
		}
		return true;
	}

	private boolean r_plural() {
		int among_var;
		// (, line 141
		// [, line 142
		ket = cursor;
		// substring, line 142
		among_var = find_among_b(a_8);
		if (among_var == 0) {
			return false;
		}
		// ], line 142
		bra = cursor;
		// call R1, line 142
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 143
			// <-, line 143
			slice_from("a");
			break;
		case 2:
			// (, line 144
			// <-, line 144
			slice_from("e");
			break;
		case 3:
			// (, line 145
			// delete, line 145
			slice_del();
			break;
		case 4:
			// (, line 146
			// delete, line 146
			slice_del();
			break;
		case 5:
			// (, line 147
			// delete, line 147
			slice_del();
			break;
		case 6:
			// (, line 148
			// delete, line 148
			slice_del();
			break;
		case 7:
			// (, line 149
			// delete, line 149
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_owned() {
		int among_var;
		// (, line 153
		// [, line 154
		ket = cursor;
		// substring, line 154
		among_var = find_among_b(a_9);
		if (among_var == 0) {
			return false;
		}
		// ], line 154
		bra = cursor;
		// call R1, line 154
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 155
			// delete, line 155
			slice_del();
			break;
		case 2:
			// (, line 156
			// <-, line 156
			slice_from("e");
			break;
		case 3:
			// (, line 157
			// <-, line 157
			slice_from("a");
			break;
		case 4:
			// (, line 158
			// delete, line 158
			slice_del();
			break;
		case 5:
			// (, line 159
			// <-, line 159
			slice_from("e");
			break;
		case 6:
			// (, line 160
			// <-, line 160
			slice_from("a");
			break;
		case 7:
			// (, line 161
			// delete, line 161
			slice_del();
			break;
		case 8:
			// (, line 162
			// <-, line 162
			slice_from("e");
			break;
		case 9:
			// (, line 163
			// delete, line 163
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_sing_owner() {
		int among_var;
		// (, line 167
		// [, line 168
		ket = cursor;
		// substring, line 168
		among_var = find_among_b(a_10);
		if (among_var == 0) {
			return false;
		}
		// ], line 168
		bra = cursor;
		// call R1, line 168
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 169
			// delete, line 169
			slice_del();
			break;
		case 2:
			// (, line 170
			// <-, line 170
			slice_from("a");
			break;
		case 3:
			// (, line 171
			// <-, line 171
			slice_from("e");
			break;
		case 4:
			// (, line 172
			// delete, line 172
			slice_del();
			break;
		case 5:
			// (, line 173
			// <-, line 173
			slice_from("a");
			break;
		case 6:
			// (, line 174
			// <-, line 174
			slice_from("e");
			break;
		case 7:
			// (, line 175
			// delete, line 175
			slice_del();
			break;
		case 8:
			// (, line 176
			// delete, line 176
			slice_del();
			break;
		case 9:
			// (, line 177
			// delete, line 177
			slice_del();
			break;
		case 10:
			// (, line 178
			// <-, line 178
			slice_from("a");
			break;
		case 11:
			// (, line 179
			// <-, line 179
			slice_from("e");
			break;
		case 12:
			// (, line 180
			// delete, line 180
			slice_del();
			break;
		case 13:
			// (, line 181
			// delete, line 181
			slice_del();
			break;
		case 14:
			// (, line 182
			// <-, line 182
			slice_from("a");
			break;
		case 15:
			// (, line 183
			// <-, line 183
			slice_from("e");
			break;
		case 16:
			// (, line 184
			// delete, line 184
			slice_del();
			break;
		case 17:
			// (, line 185
			// delete, line 185
			slice_del();
			break;
		case 18:
			// (, line 186
			// delete, line 186
			slice_del();
			break;
		case 19:
			// (, line 187
			// <-, line 187
			slice_from("a");
			break;
		case 20:
			// (, line 188
			// <-, line 188
			slice_from("e");
			break;
		}
		return true;
	}

	private boolean r_plur_owner() {
		int among_var;
		// (, line 192
		// [, line 193
		ket = cursor;
		// substring, line 193
		among_var = find_among_b(a_11);
		if (among_var == 0) {
			return false;
		}
		// ], line 193
		bra = cursor;
		// call R1, line 193
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 194
			// delete, line 194
			slice_del();
			break;
		case 2:
			// (, line 195
			// <-, line 195
			slice_from("a");
			break;
		case 3:
			// (, line 196
			// <-, line 196
			slice_from("e");
			break;
		case 4:
			// (, line 197
			// delete, line 197
			slice_del();
			break;
		case 5:
			// (, line 198
			// delete, line 198
			slice_del();
			break;
		case 6:
			// (, line 199
			// delete, line 199
			slice_del();
			break;
		case 7:
			// (, line 200
			// <-, line 200
			slice_from("a");
			break;
		case 8:
			// (, line 201
			// <-, line 201
			slice_from("e");
			break;
		case 9:
			// (, line 202
			// delete, line 202
			slice_del();
			break;
		case 10:
			// (, line 203
			// delete, line 203
			slice_del();
			break;
		case 11:
			// (, line 204
			// delete, line 204
			slice_del();
			break;
		case 12:
			// (, line 205
			// <-, line 205
			slice_from("a");
			break;
		case 13:
			// (, line 206
			// <-, line 206
			slice_from("e");
			break;
		case 14:
			// (, line 207
			// delete, line 207
			slice_del();
			break;
		case 15:
			// (, line 208
			// delete, line 208
			slice_del();
			break;
		case 16:
			// (, line 209
			// delete, line 209
			slice_del();
			break;
		case 17:
			// (, line 210
			// delete, line 210
			slice_del();
			break;
		case 18:
			// (, line 211
			// <-, line 211
			slice_from("a");
			break;
		case 19:
			// (, line 212
			// <-, line 212
			slice_from("e");
			break;
		case 20:
			// (, line 214
			// delete, line 214
			slice_del();
			break;
		case 21:
			// (, line 215
			// delete, line 215
			slice_del();
			break;
		case 22:
			// (, line 216
			// <-, line 216
			slice_from("a");
			break;
		case 23:
			// (, line 217
			// <-, line 217
			slice_from("e");
			break;
		case 24:
			// (, line 218
			// delete, line 218
			slice_del();
			break;
		case 25:
			// (, line 219
			// delete, line 219
			slice_del();
			break;
		case 26:
			// (, line 220
			// delete, line 220
			slice_del();
			break;
		case 27:
			// (, line 221
			// <-, line 221
			slice_from("a");
			break;
		case 28:
			// (, line 222
			// <-, line 222
			slice_from("e");
			break;
		case 29:
			// (, line 223
			// delete, line 223
			slice_del();
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 228
		// do, line 229
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 229
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 230
		limit_backward = cursor;
		cursor = limit;
		// (, line 230
		// do, line 231
		v_2 = limit - cursor;
		lab1: do {
			// call instrum, line 231
			if (!r_instrum()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 232
		v_3 = limit - cursor;
		lab2: do {
			// call case, line 232
			if (!r_case()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 233
		v_4 = limit - cursor;
		lab3: do {
			// call case_special, line 233
			if (!r_case_special()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		// do, line 234
		v_5 = limit - cursor;
		lab4: do {
			// call case_other, line 234
			if (!r_case_other()) {
				break lab4;
			}
		} while (false);
		cursor = limit - v_5;
		// do, line 235
		v_6 = limit - cursor;
		lab5: do {
			// call factive, line 235
			if (!r_factive()) {
				break lab5;
			}
		} while (false);
		cursor = limit - v_6;
		// do, line 236
		v_7 = limit - cursor;
		lab6: do {
			// call owned, line 236
			if (!r_owned()) {
				break lab6;
			}
		} while (false);
		cursor = limit - v_7;
		// do, line 237
		v_8 = limit - cursor;
		lab7: do {
			// call sing_owner, line 237
			if (!r_sing_owner()) {
				break lab7;
			}
		} while (false);
		cursor = limit - v_8;
		// do, line 238
		v_9 = limit - cursor;
		lab8: do {
			// call plur_owner, line 238
			if (!r_plur_owner()) {
				break lab8;
			}
		} while (false);
		cursor = limit - v_9;
		// do, line 239
		v_10 = limit - cursor;
		lab9: do {
			// call plural, line 239
			if (!r_plural()) {
				break lab9;
			}
		} while (false);
		cursor = limit - v_10;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class irishStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("b'", -1, 4), new Among("bh", -1, 14), new Among("bhf", 1, 9),
			new Among("bp", -1, 11), new Among("ch", -1, 15), new Among("d'", -1, 2), new Among("d'fh", 5, 3),
			new Among("dh", -1, 16), new Among("dt", -1, 13), new Among("fh", -1, 17), new Among("gc", -1, 7),
			new Among("gh", -1, 18), new Among("h-", -1, 1), new Among("m'", -1, 4), new Among("mb", -1, 6),
			new Among("mh", -1, 19), new Among("n-", -1, 1), new Among("nd", -1, 8), new Among("ng", -1, 10),
			new Among("ph", -1, 20), new Among("sh", -1, 5), new Among("t-", -1, 1), new Among("th", -1, 21),
			new Among("ts", -1, 12) };

	private final static Among a_1[] = { new Among("\u00EDochta", -1, 1), new Among("a\u00EDochta", 0, 1),
			new Among("ire", -1, 2), new Among("aire", 2, 2), new Among("abh", -1, 1), new Among("eabh", 4, 1),
			new Among("ibh", -1, 1), new Among("aibh", 6, 1), new Among("amh", -1, 1), new Among("eamh", 8, 1),
			new Among("imh", -1, 1), new Among("aimh", 10, 1), new Among("\u00EDocht", -1, 1),
			new Among("a\u00EDocht", 12, 1), new Among("ir\u00ED", -1, 2), new Among("air\u00ED", 14, 2) };

	private final static Among a_2[] = { new Among("\u00F3ideacha", -1, 6), new Among("patacha", -1, 5),
			new Among("achta", -1, 1), new Among("arcachta", 2, 2), new Among("eachta", 2, 1),
			new Among("grafa\u00EDochta", -1, 4), new Among("paite", -1, 5), new Among("ach", -1, 1),
			new Among("each", 7, 1), new Among("\u00F3ideach", 8, 6), new Among("gineach", 8, 3),
			new Among("patach", 7, 5), new Among("grafa\u00EDoch", -1, 4), new Among("pataigh", -1, 5),
			new Among("\u00F3idigh", -1, 6), new Among("acht\u00FAil", -1, 1), new Among("eacht\u00FAil", 15, 1),
			new Among("gineas", -1, 3), new Among("ginis", -1, 3), new Among("acht", -1, 1),
			new Among("arcacht", 19, 2), new Among("eacht", 19, 1), new Among("grafa\u00EDocht", -1, 4),
			new Among("arcachta\u00ED", -1, 2), new Among("grafa\u00EDochta\u00ED", -1, 4) };

	private final static Among a_3[] = { new Among("imid", -1, 1), new Among("aimid", 0, 1),
			new Among("\u00EDmid", -1, 1), new Among("a\u00EDmid", 2, 1), new Among("adh", -1, 2),
			new Among("eadh", 4, 2), new Among("faidh", -1, 1), new Among("fidh", -1, 1), new Among("\u00E1il", -1, 2),
			new Among("ain", -1, 2), new Among("tear", -1, 2), new Among("tar", -1, 2) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 4, 2 };

	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_mark_regions() {
		int v_1;
		int v_3;
		// (, line 28
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 34
		v_1 = cursor;
		lab0: do {
			// (, line 34
			// gopast, line 35
			golab1: while (true) {
				lab2: do {
					if (!(in_grouping(g_v, 97, 250))) {
						break lab2;
					}
					break golab1;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark pV, line 35
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 37
		v_3 = cursor;
		lab3: do {
			// (, line 37
			// gopast, line 38
			golab4: while (true) {
				lab5: do {
					if (!(in_grouping(g_v, 97, 250))) {
						break lab5;
					}
					break golab4;
				} while (false);
				if (cursor >= limit) {
					break lab3;
				}
				cursor++;
			}
			// gopast, line 38
			golab6: while (true) {
				lab7: do {
					if (!(out_grouping(g_v, 97, 250))) {
						break lab7;
					}
					break golab6;
				} while (false);
				if (cursor >= limit) {
					break lab3;
				}
				cursor++;
			}
			// setmark p1, line 38
			I_p1 = cursor;
			// gopast, line 39
			golab8: while (true) {
				lab9: do {
					if (!(in_grouping(g_v, 97, 250))) {
						break lab9;
					}
					break golab8;
				} while (false);
				if (cursor >= limit) {
					break lab3;
				}
				cursor++;
			}
			// gopast, line 39
			golab10: while (true) {
				lab11: do {
					if (!(out_grouping(g_v, 97, 250))) {
						break lab11;
					}
					break golab10;
				} while (false);
				if (cursor >= limit) {
					break lab3;
				}
				cursor++;
			}
			// setmark p2, line 39
			I_p2 = cursor;
		} while (false);
		cursor = v_3;
		return true;
	}

	private boolean r_initial_morph() {
		int among_var;
		// (, line 43
		// [, line 44
		bra = cursor;
		// substring, line 44
		among_var = find_among(a_0);
		if (among_var == 0) {
			return false;
		}
		// ], line 44
		ket = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 46
			// delete, line 46
			slice_del();
			break;
		case 2:
			// (, line 50
			// delete, line 50
			slice_del();
			break;
		case 3:
			// (, line 52
			// <-, line 52
			slice_from("f");
			break;
		case 4:
			// (, line 55
			// delete, line 55
			slice_del();
			break;
		case 5:
			// (, line 58
			// <-, line 58
			slice_from("s");
			break;
		case 6:
			// (, line 61
			// <-, line 61
			slice_from("b");
			break;
		case 7:
			// (, line 63
			// <-, line 63
			slice_from("c");
			break;
		case 8:
			// (, line 65
			// <-, line 65
			slice_from("d");
			break;
		case 9:
			// (, line 67
			// <-, line 67
			slice_from("f");
			break;
		case 10:
			// (, line 69
			// <-, line 69
			slice_from("g");
			break;
		case 11:
			// (, line 71
			// <-, line 71
			slice_from("p");
			break;
		case 12:
			// (, line 73
			// <-, line 73
			slice_from("s");
			break;
		case 13:
			// (, line 75
			// <-, line 75
			slice_from("t");
			break;
		case 14:
			// (, line 79
			// <-, line 79
			slice_from("b");
			break;
		case 15:
			// (, line 81
			// <-, line 81
			slice_from("c");
			break;
		case 16:
			// (, line 83
			// <-, line 83
			slice_from("d");
			break;
		case 17:
			// (, line 85
			// <-, line 85
			slice_from("f");
			break;
		case 18:
			// (, line 87
			// <-, line 87
			slice_from("g");
			break;
		case 19:
			// (, line 89
			// <-, line 89
			slice_from("m");
			break;
		case 20:
			// (, line 91
			// <-, line 91
			slice_from("p");
			break;
		case 21:
			// (, line 93
			// <-, line 93
			slice_from("t");
			break;
		}
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_noun_sfx() {
		int among_var;
		// (, line 103
		// [, line 104
		ket = cursor;
		// substring, line 104
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 104
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 108
			// call R1, line 108
			if (!r_R1()) {
				return false;
			}
			// delete, line 108
			slice_del();
			break;
		case 2:
			// (, line 110
			// call R2, line 110
			if (!r_R2()) {
				return false;
			}
			// delete, line 110
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_deriv() {
		int among_var;
		// (, line 113
		// [, line 114
		ket = cursor;
		// substring, line 114
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 114
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 116
			// call R2, line 116
			if (!r_R2()) {
				return false;
			}
			// delete, line 116
			slice_del();
			break;
		case 2:
			// (, line 118
			// <-, line 118
			slice_from("arc");
			break;
		case 3:
			// (, line 120
			// <-, line 120
			slice_from("gin");
			break;
		case 4:
			// (, line 122
			// <-, line 122
			slice_from("graf");
			break;
		case 5:
			// (, line 124
			// <-, line 124
			slice_from("paite");
			break;
		case 6:
			// (, line 126
			// <-, line 126
			slice_from("\u00F3id");
			break;
		}
		return true;
	}

	private boolean r_verb_sfx() {
		int among_var;
		// (, line 129
		// [, line 130
		ket = cursor;
		// substring, line 130
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 130
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 133
			// call RV, line 133
			if (!r_RV()) {
				return false;
			}
			// delete, line 133
			slice_del();
			break;
		case 2:
			// (, line 138
			// call R1, line 138
			if (!r_R1()) {
				return false;
			}
			// delete, line 138
			slice_del();
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 143
		// do, line 144
		v_1 = cursor;
		lab0: do {
			// call initial_morph, line 144
			if (!r_initial_morph()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 145
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 145
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 146
		limit_backward = cursor;
		cursor = limit;
		// (, line 146
		// do, line 147
		v_3 = limit - cursor;
		lab2: do {
			// call noun_sfx, line 147
			if (!r_noun_sfx()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 148
		v_4 = limit - cursor;
		lab3: do {
			// call deriv, line 148
			if (!r_deriv()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		// do, line 149
		v_5 = limit - cursor;
		lab4: do {
			// call verb_sfx, line 149
			if (!r_verb_sfx()) {
				break lab4;
			}
		} while (false);
		cursor = limit - v_5;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class italianStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 7), new Among("qu", 0, 6), new Among("\u00E1", 0, 1),
			new Among("\u00E9", 0, 2), new Among("\u00ED", 0, 3), new Among("\u00F3", 0, 4),
			new Among("\u00FA", 0, 5) };

	private final static Among a_1[] = { new Among("", -1, 3), new Among("I", 0, 1), new Among("U", 0, 2) };

	private final static Among a_2[] = { new Among("la", -1, -1), new Among("cela", 0, -1), new Among("gliela", 0, -1),
			new Among("mela", 0, -1), new Among("tela", 0, -1), new Among("vela", 0, -1), new Among("le", -1, -1),
			new Among("cele", 6, -1), new Among("gliele", 6, -1), new Among("mele", 6, -1), new Among("tele", 6, -1),
			new Among("vele", 6, -1), new Among("ne", -1, -1), new Among("cene", 12, -1), new Among("gliene", 12, -1),
			new Among("mene", 12, -1), new Among("sene", 12, -1), new Among("tene", 12, -1), new Among("vene", 12, -1),
			new Among("ci", -1, -1), new Among("li", -1, -1), new Among("celi", 20, -1), new Among("glieli", 20, -1),
			new Among("meli", 20, -1), new Among("teli", 20, -1), new Among("veli", 20, -1), new Among("gli", 20, -1),
			new Among("mi", -1, -1), new Among("si", -1, -1), new Among("ti", -1, -1), new Among("vi", -1, -1),
			new Among("lo", -1, -1), new Among("celo", 31, -1), new Among("glielo", 31, -1), new Among("melo", 31, -1),
			new Among("telo", 31, -1), new Among("velo", 31, -1) };

	private final static Among a_3[] = { new Among("ando", -1, 1), new Among("endo", -1, 1), new Among("ar", -1, 2),
			new Among("er", -1, 2), new Among("ir", -1, 2) };

	private final static Among a_4[] = { new Among("ic", -1, -1), new Among("abil", -1, -1), new Among("os", -1, -1),
			new Among("iv", -1, 1) };

	private final static Among a_5[] = { new Among("ic", -1, 1), new Among("abil", -1, 1), new Among("iv", -1, 1) };

	private final static Among a_6[] = { new Among("ica", -1, 1), new Among("logia", -1, 3), new Among("osa", -1, 1),
			new Among("ista", -1, 1), new Among("iva", -1, 9), new Among("anza", -1, 1), new Among("enza", -1, 5),
			new Among("ice", -1, 1), new Among("atrice", 7, 1), new Among("iche", -1, 1), new Among("logie", -1, 3),
			new Among("abile", -1, 1), new Among("ibile", -1, 1), new Among("usione", -1, 4),
			new Among("azione", -1, 2), new Among("uzione", -1, 4), new Among("atore", -1, 2), new Among("ose", -1, 1),
			new Among("ante", -1, 1), new Among("mente", -1, 1), new Among("amente", 19, 7), new Among("iste", -1, 1),
			new Among("ive", -1, 9), new Among("anze", -1, 1), new Among("enze", -1, 5), new Among("ici", -1, 1),
			new Among("atrici", 25, 1), new Among("ichi", -1, 1), new Among("abili", -1, 1), new Among("ibili", -1, 1),
			new Among("ismi", -1, 1), new Among("usioni", -1, 4), new Among("azioni", -1, 2),
			new Among("uzioni", -1, 4), new Among("atori", -1, 2), new Among("osi", -1, 1), new Among("anti", -1, 1),
			new Among("amenti", -1, 6), new Among("imenti", -1, 6), new Among("isti", -1, 1), new Among("ivi", -1, 9),
			new Among("ico", -1, 1), new Among("ismo", -1, 1), new Among("oso", -1, 1), new Among("amento", -1, 6),
			new Among("imento", -1, 6), new Among("ivo", -1, 9), new Among("it\u00E0", -1, 8),
			new Among("ist\u00E0", -1, 1), new Among("ist\u00E8", -1, 1), new Among("ist\u00EC", -1, 1) };

	private final static Among a_7[] = { new Among("isca", -1, 1), new Among("enda", -1, 1), new Among("ata", -1, 1),
			new Among("ita", -1, 1), new Among("uta", -1, 1), new Among("ava", -1, 1), new Among("eva", -1, 1),
			new Among("iva", -1, 1), new Among("erebbe", -1, 1), new Among("irebbe", -1, 1), new Among("isce", -1, 1),
			new Among("ende", -1, 1), new Among("are", -1, 1), new Among("ere", -1, 1), new Among("ire", -1, 1),
			new Among("asse", -1, 1), new Among("ate", -1, 1), new Among("avate", 16, 1), new Among("evate", 16, 1),
			new Among("ivate", 16, 1), new Among("ete", -1, 1), new Among("erete", 20, 1), new Among("irete", 20, 1),
			new Among("ite", -1, 1), new Among("ereste", -1, 1), new Among("ireste", -1, 1), new Among("ute", -1, 1),
			new Among("erai", -1, 1), new Among("irai", -1, 1), new Among("isci", -1, 1), new Among("endi", -1, 1),
			new Among("erei", -1, 1), new Among("irei", -1, 1), new Among("assi", -1, 1), new Among("ati", -1, 1),
			new Among("iti", -1, 1), new Among("eresti", -1, 1), new Among("iresti", -1, 1), new Among("uti", -1, 1),
			new Among("avi", -1, 1), new Among("evi", -1, 1), new Among("ivi", -1, 1), new Among("isco", -1, 1),
			new Among("ando", -1, 1), new Among("endo", -1, 1), new Among("Yamo", -1, 1), new Among("iamo", -1, 1),
			new Among("avamo", -1, 1), new Among("evamo", -1, 1), new Among("ivamo", -1, 1), new Among("eremo", -1, 1),
			new Among("iremo", -1, 1), new Among("assimo", -1, 1), new Among("ammo", -1, 1), new Among("emmo", -1, 1),
			new Among("eremmo", 54, 1), new Among("iremmo", 54, 1), new Among("immo", -1, 1), new Among("ano", -1, 1),
			new Among("iscano", 58, 1), new Among("avano", 58, 1), new Among("evano", 58, 1), new Among("ivano", 58, 1),
			new Among("eranno", -1, 1), new Among("iranno", -1, 1), new Among("ono", -1, 1), new Among("iscono", 65, 1),
			new Among("arono", 65, 1), new Among("erono", 65, 1), new Among("irono", 65, 1),
			new Among("erebbero", -1, 1), new Among("irebbero", -1, 1), new Among("assero", -1, 1),
			new Among("essero", -1, 1), new Among("issero", -1, 1), new Among("ato", -1, 1), new Among("ito", -1, 1),
			new Among("uto", -1, 1), new Among("avo", -1, 1), new Among("evo", -1, 1), new Among("ivo", -1, 1),
			new Among("ar", -1, 1), new Among("ir", -1, 1), new Among("er\u00E0", -1, 1), new Among("ir\u00E0", -1, 1),
			new Among("er\u00F2", -1, 1), new Among("ir\u00F2", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 128, 8, 2, 1 };

	private static final char g_AEIO[] = { 17, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 128, 8, 2 };

	private static final char g_CG[] = { 17 };

	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_prelude() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 34
		// test, line 35
		v_1 = cursor;
		// repeat, line 35
		replab0: while (true) {
			v_2 = cursor;
			lab1: do {
				// (, line 35
				// [, line 36
				bra = cursor;
				// substring, line 36
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 36
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 37
					// <-, line 37
					slice_from("\u00E0");
					break;
				case 2:
					// (, line 38
					// <-, line 38
					slice_from("\u00E8");
					break;
				case 3:
					// (, line 39
					// <-, line 39
					slice_from("\u00EC");
					break;
				case 4:
					// (, line 40
					// <-, line 40
					slice_from("\u00F2");
					break;
				case 5:
					// (, line 41
					// <-, line 41
					slice_from("\u00F9");
					break;
				case 6:
					// (, line 42
					// <-, line 42
					slice_from("qU");
					break;
				case 7:
					// (, line 43
					// next, line 43
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_2;
			break replab0;
		}
		cursor = v_1;
		// repeat, line 46
		replab2: while (true) {
			v_3 = cursor;
			lab3: do {
				// goto, line 46
				golab4: while (true) {
					v_4 = cursor;
					lab5: do {
						// (, line 46
						if (!(in_grouping(g_v, 97, 249))) {
							break lab5;
						}
						// [, line 47
						bra = cursor;
						// or, line 47
						lab6: do {
							v_5 = cursor;
							lab7: do {
								// (, line 47
								// literal, line 47
								if (!(eq_s("u"))) {
									break lab7;
								}
								// ], line 47
								ket = cursor;
								if (!(in_grouping(g_v, 97, 249))) {
									break lab7;
								}
								// <-, line 47
								slice_from("U");
								break lab6;
							} while (false);
							cursor = v_5;
							// (, line 48
							// literal, line 48
							if (!(eq_s("i"))) {
								break lab5;
							}
							// ], line 48
							ket = cursor;
							if (!(in_grouping(g_v, 97, 249))) {
								break lab5;
							}
							// <-, line 48
							slice_from("I");
						} while (false);
						cursor = v_4;
						break golab4;
					} while (false);
					cursor = v_4;
					if (cursor >= limit) {
						break lab3;
					}
					cursor++;
				}
				continue replab2;
			} while (false);
			cursor = v_3;
			break replab2;
		}
		return true;
	}

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_3;
		int v_6;
		int v_8;
		// (, line 52
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 58
		v_1 = cursor;
		lab0: do {
			// (, line 58
			// or, line 60
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// (, line 59
					if (!(in_grouping(g_v, 97, 249))) {
						break lab2;
					}
					// or, line 59
					lab3: do {
						v_3 = cursor;
						lab4: do {
							// (, line 59
							if (!(out_grouping(g_v, 97, 249))) {
								break lab4;
							}
							// gopast, line 59
							golab5: while (true) {
								lab6: do {
									if (!(in_grouping(g_v, 97, 249))) {
										break lab6;
									}
									break golab5;
								} while (false);
								if (cursor >= limit) {
									break lab4;
								}
								cursor++;
							}
							break lab3;
						} while (false);
						cursor = v_3;
						// (, line 59
						if (!(in_grouping(g_v, 97, 249))) {
							break lab2;
						}
						// gopast, line 59
						golab7: while (true) {
							lab8: do {
								if (!(out_grouping(g_v, 97, 249))) {
									break lab8;
								}
								break golab7;
							} while (false);
							if (cursor >= limit) {
								break lab2;
							}
							cursor++;
						}
					} while (false);
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 61
				if (!(out_grouping(g_v, 97, 249))) {
					break lab0;
				}
				// or, line 61
				lab9: do {
					v_6 = cursor;
					lab10: do {
						// (, line 61
						if (!(out_grouping(g_v, 97, 249))) {
							break lab10;
						}
						// gopast, line 61
						golab11: while (true) {
							lab12: do {
								if (!(in_grouping(g_v, 97, 249))) {
									break lab12;
								}
								break golab11;
							} while (false);
							if (cursor >= limit) {
								break lab10;
							}
							cursor++;
						}
						break lab9;
					} while (false);
					cursor = v_6;
					// (, line 61
					if (!(in_grouping(g_v, 97, 249))) {
						break lab0;
					}
					// next, line 61
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				} while (false);
			} while (false);
			// setmark pV, line 62
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 64
		v_8 = cursor;
		lab13: do {
			// (, line 64
			// gopast, line 65
			golab14: while (true) {
				lab15: do {
					if (!(in_grouping(g_v, 97, 249))) {
						break lab15;
					}
					break golab14;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 65
			golab16: while (true) {
				lab17: do {
					if (!(out_grouping(g_v, 97, 249))) {
						break lab17;
					}
					break golab16;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p1, line 65
			I_p1 = cursor;
			// gopast, line 66
			golab18: while (true) {
				lab19: do {
					if (!(in_grouping(g_v, 97, 249))) {
						break lab19;
					}
					break golab18;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 66
			golab20: while (true) {
				lab21: do {
					if (!(out_grouping(g_v, 97, 249))) {
						break lab21;
					}
					break golab20;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p2, line 66
			I_p2 = cursor;
		} while (false);
		cursor = v_8;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 70
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 70
				// [, line 72
				bra = cursor;
				// substring, line 72
				among_var = find_among(a_1);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 72
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 73
					// <-, line 73
					slice_from("i");
					break;
				case 2:
					// (, line 74
					// <-, line 74
					slice_from("u");
					break;
				case 3:
					// (, line 75
					// next, line 75
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_attached_pronoun() {
		int among_var;
		// (, line 86
		// [, line 87
		ket = cursor;
		// substring, line 87
		if (find_among_b(a_2) == 0) {
			return false;
		}
		// ], line 87
		bra = cursor;
		// among, line 97
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// (, line 97
		// call RV, line 97
		if (!r_RV()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 98
			// delete, line 98
			slice_del();
			break;
		case 2:
			// (, line 99
			// <-, line 99
			slice_from("e");
			break;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 103
		// [, line 104
		ket = cursor;
		// substring, line 104
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 104
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 111
			// call R2, line 111
			if (!r_R2()) {
				return false;
			}
			// delete, line 111
			slice_del();
			break;
		case 2:
			// (, line 113
			// call R2, line 113
			if (!r_R2()) {
				return false;
			}
			// delete, line 113
			slice_del();
			// try, line 114
			v_1 = limit - cursor;
			lab0: do {
				// (, line 114
				// [, line 114
				ket = cursor;
				// literal, line 114
				if (!(eq_s_b("ic"))) {
					cursor = limit - v_1;
					break lab0;
				}
				// ], line 114
				bra = cursor;
				// call R2, line 114
				if (!r_R2()) {
					cursor = limit - v_1;
					break lab0;
				}
				// delete, line 114
				slice_del();
			} while (false);
			break;
		case 3:
			// (, line 117
			// call R2, line 117
			if (!r_R2()) {
				return false;
			}
			// <-, line 117
			slice_from("log");
			break;
		case 4:
			// (, line 119
			// call R2, line 119
			if (!r_R2()) {
				return false;
			}
			// <-, line 119
			slice_from("u");
			break;
		case 5:
			// (, line 121
			// call R2, line 121
			if (!r_R2()) {
				return false;
			}
			// <-, line 121
			slice_from("ente");
			break;
		case 6:
			// (, line 123
			// call RV, line 123
			if (!r_RV()) {
				return false;
			}
			// delete, line 123
			slice_del();
			break;
		case 7:
			// (, line 124
			// call R1, line 125
			if (!r_R1()) {
				return false;
			}
			// delete, line 125
			slice_del();
			// try, line 126
			v_2 = limit - cursor;
			lab1: do {
				// (, line 126
				// [, line 127
				ket = cursor;
				// substring, line 127
				among_var = find_among_b(a_4);
				if (among_var == 0) {
					cursor = limit - v_2;
					break lab1;
				}
				// ], line 127
				bra = cursor;
				// call R2, line 127
				if (!r_R2()) {
					cursor = limit - v_2;
					break lab1;
				}
				// delete, line 127
				slice_del();
				switch (among_var) {
				case 0:
					cursor = limit - v_2;
					break lab1;
				case 1:
					// (, line 128
					// [, line 128
					ket = cursor;
					// literal, line 128
					if (!(eq_s_b("at"))) {
						cursor = limit - v_2;
						break lab1;
					}
					// ], line 128
					bra = cursor;
					// call R2, line 128
					if (!r_R2()) {
						cursor = limit - v_2;
						break lab1;
					}
					// delete, line 128
					slice_del();
					break;
				}
			} while (false);
			break;
		case 8:
			// (, line 133
			// call R2, line 134
			if (!r_R2()) {
				return false;
			}
			// delete, line 134
			slice_del();
			// try, line 135
			v_3 = limit - cursor;
			lab2: do {
				// (, line 135
				// [, line 136
				ket = cursor;
				// substring, line 136
				among_var = find_among_b(a_5);
				if (among_var == 0) {
					cursor = limit - v_3;
					break lab2;
				}
				// ], line 136
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_3;
					break lab2;
				case 1:
					// (, line 137
					// call R2, line 137
					if (!r_R2()) {
						cursor = limit - v_3;
						break lab2;
					}
					// delete, line 137
					slice_del();
					break;
				}
			} while (false);
			break;
		case 9:
			// (, line 141
			// call R2, line 142
			if (!r_R2()) {
				return false;
			}
			// delete, line 142
			slice_del();
			// try, line 143
			v_4 = limit - cursor;
			lab3: do {
				// (, line 143
				// [, line 143
				ket = cursor;
				// literal, line 143
				if (!(eq_s_b("at"))) {
					cursor = limit - v_4;
					break lab3;
				}
				// ], line 143
				bra = cursor;
				// call R2, line 143
				if (!r_R2()) {
					cursor = limit - v_4;
					break lab3;
				}
				// delete, line 143
				slice_del();
				// [, line 143
				ket = cursor;
				// literal, line 143
				if (!(eq_s_b("ic"))) {
					cursor = limit - v_4;
					break lab3;
				}
				// ], line 143
				bra = cursor;
				// call R2, line 143
				if (!r_R2()) {
					cursor = limit - v_4;
					break lab3;
				}
				// delete, line 143
				slice_del();
			} while (false);
			break;
		}
		return true;
	}

	private boolean r_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// setlimit, line 148
		v_1 = limit - cursor;
		// tomark, line 148
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 148
		// [, line 149
		ket = cursor;
		// substring, line 149
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 149
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_2;
			return false;
		case 1:
			// (, line 163
			// delete, line 163
			slice_del();
			break;
		}
		limit_backward = v_2;
		return true;
	}

	private boolean r_vowel_suffix() {
		int v_1;
		int v_2;
		// (, line 170
		// try, line 171
		v_1 = limit - cursor;
		lab0: do {
			// (, line 171
			// [, line 172
			ket = cursor;
			if (!(in_grouping_b(g_AEIO, 97, 242))) {
				cursor = limit - v_1;
				break lab0;
			}
			// ], line 172
			bra = cursor;
			// call RV, line 172
			if (!r_RV()) {
				cursor = limit - v_1;
				break lab0;
			}
			// delete, line 172
			slice_del();
			// [, line 173
			ket = cursor;
			// literal, line 173
			if (!(eq_s_b("i"))) {
				cursor = limit - v_1;
				break lab0;
			}
			// ], line 173
			bra = cursor;
			// call RV, line 173
			if (!r_RV()) {
				cursor = limit - v_1;
				break lab0;
			}
			// delete, line 173
			slice_del();
		} while (false);
		// try, line 175
		v_2 = limit - cursor;
		lab1: do {
			// (, line 175
			// [, line 176
			ket = cursor;
			// literal, line 176
			if (!(eq_s_b("h"))) {
				cursor = limit - v_2;
				break lab1;
			}
			// ], line 176
			bra = cursor;
			if (!(in_grouping_b(g_CG, 99, 103))) {
				cursor = limit - v_2;
				break lab1;
			}
			// call RV, line 176
			if (!r_RV()) {
				cursor = limit - v_2;
				break lab1;
			}
			// delete, line 176
			slice_del();
		} while (false);
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		// (, line 181
		// do, line 182
		v_1 = cursor;
		lab0: do {
			// call prelude, line 182
			if (!r_prelude()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 183
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 183
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 184
		limit_backward = cursor;
		cursor = limit;
		// (, line 184
		// do, line 185
		v_3 = limit - cursor;
		lab2: do {
			// call attached_pronoun, line 185
			if (!r_attached_pronoun()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 186
		v_4 = limit - cursor;
		lab3: do {
			// (, line 186
			// or, line 186
			lab4: do {
				v_5 = limit - cursor;
				lab5: do {
					// call standard_suffix, line 186
					if (!r_standard_suffix()) {
						break lab5;
					}
					break lab4;
				} while (false);
				cursor = limit - v_5;
				// call verb_suffix, line 186
				if (!r_verb_suffix()) {
					break lab3;
				}
			} while (false);
		} while (false);
		cursor = limit - v_4;
		// do, line 187
		v_6 = limit - cursor;
		lab6: do {
			// call vowel_suffix, line 187
			if (!r_vowel_suffix()) {
				break lab6;
			}
		} while (false);
		cursor = limit - v_6;
		cursor = limit_backward; // do, line 189
		v_7 = cursor;
		lab7: do {
			// call postlude, line 189
			if (!r_postlude()) {
				break lab7;
			}
		} while (false);
		cursor = v_7;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class norwegianStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("a", -1, 1), new Among("e", -1, 1), new Among("ede", 1, 1),
			new Among("ande", 1, 1), new Among("ende", 1, 1), new Among("ane", 1, 1), new Among("ene", 1, 1),
			new Among("hetene", 6, 1), new Among("erte", 1, 3), new Among("en", -1, 1), new Among("heten", 9, 1),
			new Among("ar", -1, 1), new Among("er", -1, 1), new Among("heter", 12, 1), new Among("s", -1, 2),
			new Among("as", 14, 1), new Among("es", 14, 1), new Among("edes", 16, 1), new Among("endes", 16, 1),
			new Among("enes", 16, 1), new Among("hetenes", 19, 1), new Among("ens", 14, 1), new Among("hetens", 21, 1),
			new Among("ers", 14, 1), new Among("ets", 14, 1), new Among("et", -1, 1), new Among("het", 25, 1),
			new Among("ert", -1, 3), new Among("ast", -1, 1) };

	private final static Among a_1[] = { new Among("dt", -1, -1), new Among("vt", -1, -1) };

	private final static Among a_2[] = { new Among("leg", -1, 1), new Among("eleg", 0, 1), new Among("ig", -1, 1),
			new Among("eig", 2, 1), new Among("lig", 2, 1), new Among("elig", 4, 1), new Among("els", -1, 1),
			new Among("lov", -1, 1), new Among("elov", 7, 1), new Among("slov", 7, 1), new Among("hetslov", 9, 1) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 128 };

	private static final char g_s_ending[] = { 119, 125, 149, 1 };

	private int I_x;
	private int I_p1;

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		// (, line 26
		I_p1 = limit;
		// test, line 30
		v_1 = cursor;
		// (, line 30
		// hop, line 30
		{
			int c = cursor + 3;
			if (0 > c || c > limit) {
				return false;
			}
			cursor = c;
		}
		// setmark x, line 30
		I_x = cursor;
		cursor = v_1;
		// goto, line 31
		golab0: while (true) {
			v_2 = cursor;
			lab1: do {
				if (!(in_grouping(g_v, 97, 248))) {
					break lab1;
				}
				cursor = v_2;
				break golab0;
			} while (false);
			cursor = v_2;
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 31
		golab2: while (true) {
			lab3: do {
				if (!(out_grouping(g_v, 97, 248))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p1, line 31
		I_p1 = cursor;
		// try, line 32
		lab4: do {
			// (, line 32
			if (!(I_p1 < I_x)) {
				break lab4;
			}
			I_p1 = I_x;
		} while (false);
		return true;
	}

	private boolean r_main_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		// (, line 37
		// setlimit, line 38
		v_1 = limit - cursor;
		// tomark, line 38
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 38
		// [, line 38
		ket = cursor;
		// substring, line 38
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 38
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 44
			// delete, line 44
			slice_del();
			break;
		case 2:
			// (, line 46
			// or, line 46
			lab0: do {
				v_3 = limit - cursor;
				lab1: do {
					if (!(in_grouping_b(g_s_ending, 98, 122))) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_3;
				// (, line 46
				// literal, line 46
				if (!(eq_s_b("k"))) {
					return false;
				}
				if (!(out_grouping_b(g_v, 97, 248))) {
					return false;
				}
			} while (false);
			// delete, line 46
			slice_del();
			break;
		case 3:
			// (, line 48
			// <-, line 48
			slice_from("er");
			break;
		}
		return true;
	}

	private boolean r_consonant_pair() {
		int v_1;
		int v_2;
		int v_3;
		// (, line 52
		// test, line 53
		v_1 = limit - cursor;
		// (, line 53
		// setlimit, line 54
		v_2 = limit - cursor;
		// tomark, line 54
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_3 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_2;
		// (, line 54
		// [, line 54
		ket = cursor;
		// substring, line 54
		if (find_among_b(a_1) == 0) {
			limit_backward = v_3;
			return false;
		}
		// ], line 54
		bra = cursor;
		limit_backward = v_3;
		cursor = limit - v_1;
		// next, line 59
		if (cursor <= limit_backward) {
			return false;
		}
		cursor--;
		// ], line 59
		bra = cursor;
		// delete, line 59
		slice_del();
		return true;
	}

	private boolean r_other_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 62
		// setlimit, line 63
		v_1 = limit - cursor;
		// tomark, line 63
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 63
		// [, line 63
		ket = cursor;
		// substring, line 63
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 63
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 67
			// delete, line 67
			slice_del();
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 72
		// do, line 74
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 74
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 75
		limit_backward = cursor;
		cursor = limit;
		// (, line 75
		// do, line 76
		v_2 = limit - cursor;
		lab1: do {
			// call main_suffix, line 76
			if (!r_main_suffix()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 77
		v_3 = limit - cursor;
		lab2: do {
			// call consonant_pair, line 77
			if (!r_consonant_pair()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 78
		v_4 = limit - cursor;
		lab3: do {
			// call other_suffix, line 78
			if (!r_other_suffix()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class porterStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("s", -1, 3), new Among("ies", 0, 2), new Among("sses", 0, 1),
			new Among("ss", 0, -1) };

	private final static Among a_1[] = { new Among("", -1, 3), new Among("bb", 0, 2), new Among("dd", 0, 2),
			new Among("ff", 0, 2), new Among("gg", 0, 2), new Among("bl", 0, 1), new Among("mm", 0, 2),
			new Among("nn", 0, 2), new Among("pp", 0, 2), new Among("rr", 0, 2), new Among("at", 0, 1),
			new Among("tt", 0, 2), new Among("iz", 0, 1) };

	private final static Among a_2[] = { new Among("ed", -1, 2), new Among("eed", 0, 1), new Among("ing", -1, 2) };

	private final static Among a_3[] = { new Among("anci", -1, 3), new Among("enci", -1, 2), new Among("abli", -1, 4),
			new Among("eli", -1, 6), new Among("alli", -1, 9), new Among("ousli", -1, 12), new Among("entli", -1, 5),
			new Among("aliti", -1, 10), new Among("biliti", -1, 14), new Among("iviti", -1, 13),
			new Among("tional", -1, 1), new Among("ational", 10, 8), new Among("alism", -1, 10),
			new Among("ation", -1, 8), new Among("ization", 13, 7), new Among("izer", -1, 7), new Among("ator", -1, 8),
			new Among("iveness", -1, 13), new Among("fulness", -1, 11), new Among("ousness", -1, 12) };

	private final static Among a_4[] = { new Among("icate", -1, 2), new Among("ative", -1, 3),
			new Among("alize", -1, 1), new Among("iciti", -1, 2), new Among("ical", -1, 2), new Among("ful", -1, 3),
			new Among("ness", -1, 3) };

	private final static Among a_5[] = { new Among("ic", -1, 1), new Among("ance", -1, 1), new Among("ence", -1, 1),
			new Among("able", -1, 1), new Among("ible", -1, 1), new Among("ate", -1, 1), new Among("ive", -1, 1),
			new Among("ize", -1, 1), new Among("iti", -1, 1), new Among("al", -1, 1), new Among("ism", -1, 1),
			new Among("ion", -1, 2), new Among("er", -1, 1), new Among("ous", -1, 1), new Among("ant", -1, 1),
			new Among("ent", -1, 1), new Among("ment", 15, 1), new Among("ement", 16, 1), new Among("ou", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 1 };

	private static final char g_v_WXY[] = { 1, 17, 65, 208, 1 };

	private boolean B_Y_found;
	private int I_p2;
	private int I_p1;

	private boolean r_shortv() {
		// (, line 19
		if (!(out_grouping_b(g_v_WXY, 89, 121))) {
			return false;
		}
		if (!(in_grouping_b(g_v, 97, 121))) {
			return false;
		}
		if (!(out_grouping_b(g_v, 97, 121))) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_Step_1a() {
		int among_var;
		// (, line 24
		// [, line 25
		ket = cursor;
		// substring, line 25
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			return false;
		}
		// ], line 25
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 26
			// <-, line 26
			slice_from("ss");
			break;
		case 2:
			// (, line 27
			// <-, line 27
			slice_from("i");
			break;
		case 3:
			// (, line 29
			// delete, line 29
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_1b() {
		int among_var;
		int v_1;
		int v_3;
		int v_4;
		// (, line 33
		// [, line 34
		ket = cursor;
		// substring, line 34
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 34
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 35
			// call R1, line 35
			if (!r_R1()) {
				return false;
			}
			// <-, line 35
			slice_from("ee");
			break;
		case 2:
			// (, line 37
			// test, line 38
			v_1 = limit - cursor;
			// gopast, line 38
			golab0: while (true) {
				lab1: do {
					if (!(in_grouping_b(g_v, 97, 121))) {
						break lab1;
					}
					break golab0;
				} while (false);
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
			}
			cursor = limit - v_1;
			// delete, line 38
			slice_del();
			// test, line 39
			v_3 = limit - cursor;
			// substring, line 39
			among_var = find_among_b(a_1);
			if (among_var == 0) {
				return false;
			}
			cursor = limit - v_3;
			switch (among_var) {
			case 0:
				return false;
			case 1:
			// (, line 41
			// <+, line 41
			{
				int c = cursor;
				insert(cursor, cursor, "e");
				cursor = c;
			}
				break;
			case 2:
				// (, line 44
				// [, line 44
				ket = cursor;
				// next, line 44
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
				// ], line 44
				bra = cursor;
				// delete, line 44
				slice_del();
				break;
			case 3:
				// (, line 45
				// atmark, line 45
				if (cursor != I_p1) {
					return false;
				}
				// test, line 45
				v_4 = limit - cursor;
				// call shortv, line 45
				if (!r_shortv()) {
					return false;
				}
				cursor = limit - v_4;
			// <+, line 45
			{
				int c = cursor;
				insert(cursor, cursor, "e");
				cursor = c;
			}
				break;
			}
			break;
		}
		return true;
	}

	private boolean r_Step_1c() {
		int v_1;
		// (, line 51
		// [, line 52
		ket = cursor;
		// or, line 52
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// literal, line 52
				if (!(eq_s_b("y"))) {
					break lab1;
				}
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// literal, line 52
			if (!(eq_s_b("Y"))) {
				return false;
			}
		} while (false);
		// ], line 52
		bra = cursor;
		// gopast, line 53
		golab2: while (true) {
			lab3: do {
				if (!(in_grouping_b(g_v, 97, 121))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
		}
		// <-, line 54
		slice_from("i");
		return true;
	}

	private boolean r_Step_2() {
		int among_var;
		// (, line 57
		// [, line 58
		ket = cursor;
		// substring, line 58
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 58
		bra = cursor;
		// call R1, line 58
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 59
			// <-, line 59
			slice_from("tion");
			break;
		case 2:
			// (, line 60
			// <-, line 60
			slice_from("ence");
			break;
		case 3:
			// (, line 61
			// <-, line 61
			slice_from("ance");
			break;
		case 4:
			// (, line 62
			// <-, line 62
			slice_from("able");
			break;
		case 5:
			// (, line 63
			// <-, line 63
			slice_from("ent");
			break;
		case 6:
			// (, line 64
			// <-, line 64
			slice_from("e");
			break;
		case 7:
			// (, line 66
			// <-, line 66
			slice_from("ize");
			break;
		case 8:
			// (, line 68
			// <-, line 68
			slice_from("ate");
			break;
		case 9:
			// (, line 69
			// <-, line 69
			slice_from("al");
			break;
		case 10:
			// (, line 71
			// <-, line 71
			slice_from("al");
			break;
		case 11:
			// (, line 72
			// <-, line 72
			slice_from("ful");
			break;
		case 12:
			// (, line 74
			// <-, line 74
			slice_from("ous");
			break;
		case 13:
			// (, line 76
			// <-, line 76
			slice_from("ive");
			break;
		case 14:
			// (, line 77
			// <-, line 77
			slice_from("ble");
			break;
		}
		return true;
	}

	private boolean r_Step_3() {
		int among_var;
		// (, line 81
		// [, line 82
		ket = cursor;
		// substring, line 82
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 82
		bra = cursor;
		// call R1, line 82
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 83
			// <-, line 83
			slice_from("al");
			break;
		case 2:
			// (, line 85
			// <-, line 85
			slice_from("ic");
			break;
		case 3:
			// (, line 87
			// delete, line 87
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_4() {
		int among_var;
		int v_1;
		// (, line 91
		// [, line 92
		ket = cursor;
		// substring, line 92
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 92
		bra = cursor;
		// call R2, line 92
		if (!r_R2()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 95
			// delete, line 95
			slice_del();
			break;
		case 2:
			// (, line 96
			// or, line 96
			lab0: do {
				v_1 = limit - cursor;
				lab1: do {
					// literal, line 96
					if (!(eq_s_b("s"))) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_1;
				// literal, line 96
				if (!(eq_s_b("t"))) {
					return false;
				}
			} while (false);
			// delete, line 96
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_Step_5a() {
		int v_1;
		int v_2;
		// (, line 100
		// [, line 101
		ket = cursor;
		// literal, line 101
		if (!(eq_s_b("e"))) {
			return false;
		}
		// ], line 101
		bra = cursor;
		// or, line 102
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// call R2, line 102
				if (!r_R2()) {
					break lab1;
				}
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 102
			// call R1, line 102
			if (!r_R1()) {
				return false;
			}
			// not, line 102
			{
				v_2 = limit - cursor;
				lab2: do {
					// call shortv, line 102
					if (!r_shortv()) {
						break lab2;
					}
					return false;
				} while (false);
				cursor = limit - v_2;
			}
		} while (false);
		// delete, line 103
		slice_del();
		return true;
	}

	private boolean r_Step_5b() {
		// (, line 106
		// [, line 107
		ket = cursor;
		// literal, line 107
		if (!(eq_s_b("l"))) {
			return false;
		}
		// ], line 107
		bra = cursor;
		// call R2, line 108
		if (!r_R2()) {
			return false;
		}
		// literal, line 108
		if (!(eq_s_b("l"))) {
			return false;
		}
		// delete, line 109
		slice_del();
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		int v_14;
		int v_15;
		int v_16;
		int v_17;
		int v_18;
		int v_19;
		int v_20;
		// (, line 113
		// unset Y_found, line 115
		B_Y_found = false;
		// do, line 116
		v_1 = cursor;
		lab0: do {
			// (, line 116
			// [, line 116
			bra = cursor;
			// literal, line 116
			if (!(eq_s("y"))) {
				break lab0;
			}
			// ], line 116
			ket = cursor;
			// <-, line 116
			slice_from("Y");
			// set Y_found, line 116
			B_Y_found = true;
		} while (false);
		cursor = v_1;
		// do, line 117
		v_2 = cursor;
		lab1: do {
			// repeat, line 117
			replab2: while (true) {
				v_3 = cursor;
				lab3: do {
					// (, line 117
					// goto, line 117
					golab4: while (true) {
						v_4 = cursor;
						lab5: do {
							// (, line 117
							if (!(in_grouping(g_v, 97, 121))) {
								break lab5;
							}
							// [, line 117
							bra = cursor;
							// literal, line 117
							if (!(eq_s("y"))) {
								break lab5;
							}
							// ], line 117
							ket = cursor;
							cursor = v_4;
							break golab4;
						} while (false);
						cursor = v_4;
						if (cursor >= limit) {
							break lab3;
						}
						cursor++;
					}
					// <-, line 117
					slice_from("Y");
					// set Y_found, line 117
					B_Y_found = true;
					continue replab2;
				} while (false);
				cursor = v_3;
				break replab2;
			}
		} while (false);
		cursor = v_2;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 121
		v_5 = cursor;
		lab6: do {
			// (, line 121
			// gopast, line 122
			golab7: while (true) {
				lab8: do {
					if (!(in_grouping(g_v, 97, 121))) {
						break lab8;
					}
					break golab7;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// gopast, line 122
			golab9: while (true) {
				lab10: do {
					if (!(out_grouping(g_v, 97, 121))) {
						break lab10;
					}
					break golab9;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// setmark p1, line 122
			I_p1 = cursor;
			// gopast, line 123
			golab11: while (true) {
				lab12: do {
					if (!(in_grouping(g_v, 97, 121))) {
						break lab12;
					}
					break golab11;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// gopast, line 123
			golab13: while (true) {
				lab14: do {
					if (!(out_grouping(g_v, 97, 121))) {
						break lab14;
					}
					break golab13;
				} while (false);
				if (cursor >= limit) {
					break lab6;
				}
				cursor++;
			}
			// setmark p2, line 123
			I_p2 = cursor;
		} while (false);
		cursor = v_5;
		// backwards, line 126
		limit_backward = cursor;
		cursor = limit;
		// (, line 126
		// do, line 127
		v_10 = limit - cursor;
		lab15: do {
			// call Step_1a, line 127
			if (!r_Step_1a()) {
				break lab15;
			}
		} while (false);
		cursor = limit - v_10;
		// do, line 128
		v_11 = limit - cursor;
		lab16: do {
			// call Step_1b, line 128
			if (!r_Step_1b()) {
				break lab16;
			}
		} while (false);
		cursor = limit - v_11;
		// do, line 129
		v_12 = limit - cursor;
		lab17: do {
			// call Step_1c, line 129
			if (!r_Step_1c()) {
				break lab17;
			}
		} while (false);
		cursor = limit - v_12;
		// do, line 130
		v_13 = limit - cursor;
		lab18: do {
			// call Step_2, line 130
			if (!r_Step_2()) {
				break lab18;
			}
		} while (false);
		cursor = limit - v_13;
		// do, line 131
		v_14 = limit - cursor;
		lab19: do {
			// call Step_3, line 131
			if (!r_Step_3()) {
				break lab19;
			}
		} while (false);
		cursor = limit - v_14;
		// do, line 132
		v_15 = limit - cursor;
		lab20: do {
			// call Step_4, line 132
			if (!r_Step_4()) {
				break lab20;
			}
		} while (false);
		cursor = limit - v_15;
		// do, line 133
		v_16 = limit - cursor;
		lab21: do {
			// call Step_5a, line 133
			if (!r_Step_5a()) {
				break lab21;
			}
		} while (false);
		cursor = limit - v_16;
		// do, line 134
		v_17 = limit - cursor;
		lab22: do {
			// call Step_5b, line 134
			if (!r_Step_5b()) {
				break lab22;
			}
		} while (false);
		cursor = limit - v_17;
		cursor = limit_backward; // do, line 137
		v_18 = cursor;
		lab23: do {
			// (, line 137
			// Boolean test Y_found, line 137
			if (!(B_Y_found)) {
				break lab23;
			}
			// repeat, line 137
			replab24: while (true) {
				v_19 = cursor;
				lab25: do {
					// (, line 137
					// goto, line 137
					golab26: while (true) {
						v_20 = cursor;
						lab27: do {
							// (, line 137
							// [, line 137
							bra = cursor;
							// literal, line 137
							if (!(eq_s("Y"))) {
								break lab27;
							}
							// ], line 137
							ket = cursor;
							cursor = v_20;
							break golab26;
						} while (false);
						cursor = v_20;
						if (cursor >= limit) {
							break lab25;
						}
						cursor++;
					}
					// <-, line 137
					slice_from("y");
					continue replab24;
				} while (false);
				cursor = v_19;
				break replab24;
			}
		} while (false);
		cursor = v_18;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class portugueseStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 3), new Among("\u00E3", 0, 1), new Among("\u00F5", 0, 2) };

	private final static Among a_1[] = { new Among("", -1, 3), new Among("a~", 0, 1), new Among("o~", 0, 2) };

	private final static Among a_2[] = { new Among("ic", -1, -1), new Among("ad", -1, -1), new Among("os", -1, -1),
			new Among("iv", -1, 1) };

	private final static Among a_3[] = { new Among("ante", -1, 1), new Among("avel", -1, 1),
			new Among("\u00EDvel", -1, 1) };

	private final static Among a_4[] = { new Among("ic", -1, 1), new Among("abil", -1, 1), new Among("iv", -1, 1) };

	private final static Among a_5[] = { new Among("ica", -1, 1), new Among("\u00E2ncia", -1, 1),
			new Among("\u00EAncia", -1, 4), new Among("logia", -1, 2), new Among("ira", -1, 9),
			new Among("adora", -1, 1), new Among("osa", -1, 1), new Among("ista", -1, 1), new Among("iva", -1, 8),
			new Among("eza", -1, 1), new Among("idade", -1, 7), new Among("ante", -1, 1), new Among("mente", -1, 6),
			new Among("amente", 12, 5), new Among("\u00E1vel", -1, 1), new Among("\u00EDvel", -1, 1),
			new Among("ico", -1, 1), new Among("ismo", -1, 1), new Among("oso", -1, 1), new Among("amento", -1, 1),
			new Among("imento", -1, 1), new Among("ivo", -1, 8), new Among("a\u00E7a~o", -1, 1),
			new Among("u\u00E7a~o", -1, 3), new Among("ador", -1, 1), new Among("icas", -1, 1),
			new Among("\u00EAncias", -1, 4), new Among("logias", -1, 2), new Among("iras", -1, 9),
			new Among("adoras", -1, 1), new Among("osas", -1, 1), new Among("istas", -1, 1), new Among("ivas", -1, 8),
			new Among("ezas", -1, 1), new Among("idades", -1, 7), new Among("adores", -1, 1), new Among("antes", -1, 1),
			new Among("a\u00E7o~es", -1, 1), new Among("u\u00E7o~es", -1, 3), new Among("icos", -1, 1),
			new Among("ismos", -1, 1), new Among("osos", -1, 1), new Among("amentos", -1, 1),
			new Among("imentos", -1, 1), new Among("ivos", -1, 8) };

	private final static Among a_6[] = { new Among("ada", -1, 1), new Among("ida", -1, 1), new Among("ia", -1, 1),
			new Among("aria", 2, 1), new Among("eria", 2, 1), new Among("iria", 2, 1), new Among("ara", -1, 1),
			new Among("era", -1, 1), new Among("ira", -1, 1), new Among("ava", -1, 1), new Among("asse", -1, 1),
			new Among("esse", -1, 1), new Among("isse", -1, 1), new Among("aste", -1, 1), new Among("este", -1, 1),
			new Among("iste", -1, 1), new Among("ei", -1, 1), new Among("arei", 16, 1), new Among("erei", 16, 1),
			new Among("irei", 16, 1), new Among("am", -1, 1), new Among("iam", 20, 1), new Among("ariam", 21, 1),
			new Among("eriam", 21, 1), new Among("iriam", 21, 1), new Among("aram", 20, 1), new Among("eram", 20, 1),
			new Among("iram", 20, 1), new Among("avam", 20, 1), new Among("em", -1, 1), new Among("arem", 29, 1),
			new Among("erem", 29, 1), new Among("irem", 29, 1), new Among("assem", 29, 1), new Among("essem", 29, 1),
			new Among("issem", 29, 1), new Among("ado", -1, 1), new Among("ido", -1, 1), new Among("ando", -1, 1),
			new Among("endo", -1, 1), new Among("indo", -1, 1), new Among("ara~o", -1, 1), new Among("era~o", -1, 1),
			new Among("ira~o", -1, 1), new Among("ar", -1, 1), new Among("er", -1, 1), new Among("ir", -1, 1),
			new Among("as", -1, 1), new Among("adas", 47, 1), new Among("idas", 47, 1), new Among("ias", 47, 1),
			new Among("arias", 50, 1), new Among("erias", 50, 1), new Among("irias", 50, 1), new Among("aras", 47, 1),
			new Among("eras", 47, 1), new Among("iras", 47, 1), new Among("avas", 47, 1), new Among("es", -1, 1),
			new Among("ardes", 58, 1), new Among("erdes", 58, 1), new Among("irdes", 58, 1), new Among("ares", 58, 1),
			new Among("eres", 58, 1), new Among("ires", 58, 1), new Among("asses", 58, 1), new Among("esses", 58, 1),
			new Among("isses", 58, 1), new Among("astes", 58, 1), new Among("estes", 58, 1), new Among("istes", 58, 1),
			new Among("is", -1, 1), new Among("ais", 71, 1), new Among("eis", 71, 1), new Among("areis", 73, 1),
			new Among("ereis", 73, 1), new Among("ireis", 73, 1), new Among("\u00E1reis", 73, 1),
			new Among("\u00E9reis", 73, 1), new Among("\u00EDreis", 73, 1), new Among("\u00E1sseis", 73, 1),
			new Among("\u00E9sseis", 73, 1), new Among("\u00EDsseis", 73, 1), new Among("\u00E1veis", 73, 1),
			new Among("\u00EDeis", 73, 1), new Among("ar\u00EDeis", 84, 1), new Among("er\u00EDeis", 84, 1),
			new Among("ir\u00EDeis", 84, 1), new Among("ados", -1, 1), new Among("idos", -1, 1),
			new Among("amos", -1, 1), new Among("\u00E1ramos", 90, 1), new Among("\u00E9ramos", 90, 1),
			new Among("\u00EDramos", 90, 1), new Among("\u00E1vamos", 90, 1), new Among("\u00EDamos", 90, 1),
			new Among("ar\u00EDamos", 95, 1), new Among("er\u00EDamos", 95, 1), new Among("ir\u00EDamos", 95, 1),
			new Among("emos", -1, 1), new Among("aremos", 99, 1), new Among("eremos", 99, 1),
			new Among("iremos", 99, 1), new Among("\u00E1ssemos", 99, 1), new Among("\u00EAssemos", 99, 1),
			new Among("\u00EDssemos", 99, 1), new Among("imos", -1, 1), new Among("armos", -1, 1),
			new Among("ermos", -1, 1), new Among("irmos", -1, 1), new Among("\u00E1mos", -1, 1),
			new Among("ar\u00E1s", -1, 1), new Among("er\u00E1s", -1, 1), new Among("ir\u00E1s", -1, 1),
			new Among("eu", -1, 1), new Among("iu", -1, 1), new Among("ou", -1, 1), new Among("ar\u00E1", -1, 1),
			new Among("er\u00E1", -1, 1), new Among("ir\u00E1", -1, 1) };

	private final static Among a_7[] = { new Among("a", -1, 1), new Among("i", -1, 1), new Among("o", -1, 1),
			new Among("os", -1, 1), new Among("\u00E1", -1, 1), new Among("\u00ED", -1, 1),
			new Among("\u00F3", -1, 1) };

	private final static Among a_8[] = { new Among("e", -1, 1), new Among("\u00E7", -1, 2), new Among("\u00E9", -1, 1),
			new Among("\u00EA", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 19, 12, 2 };

	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_prelude() {
		int among_var;
		int v_1;
		// repeat, line 36
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 36
				// [, line 37
				bra = cursor;
				// substring, line 37
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 37
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 38
					// <-, line 38
					slice_from("a~");
					break;
				case 2:
					// (, line 39
					// <-, line 39
					slice_from("o~");
					break;
				case 3:
					// (, line 40
					// next, line 40
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_3;
		int v_6;
		int v_8;
		// (, line 44
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 50
		v_1 = cursor;
		lab0: do {
			// (, line 50
			// or, line 52
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// (, line 51
					if (!(in_grouping(g_v, 97, 250))) {
						break lab2;
					}
					// or, line 51
					lab3: do {
						v_3 = cursor;
						lab4: do {
							// (, line 51
							if (!(out_grouping(g_v, 97, 250))) {
								break lab4;
							}
							// gopast, line 51
							golab5: while (true) {
								lab6: do {
									if (!(in_grouping(g_v, 97, 250))) {
										break lab6;
									}
									break golab5;
								} while (false);
								if (cursor >= limit) {
									break lab4;
								}
								cursor++;
							}
							break lab3;
						} while (false);
						cursor = v_3;
						// (, line 51
						if (!(in_grouping(g_v, 97, 250))) {
							break lab2;
						}
						// gopast, line 51
						golab7: while (true) {
							lab8: do {
								if (!(out_grouping(g_v, 97, 250))) {
									break lab8;
								}
								break golab7;
							} while (false);
							if (cursor >= limit) {
								break lab2;
							}
							cursor++;
						}
					} while (false);
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 53
				if (!(out_grouping(g_v, 97, 250))) {
					break lab0;
				}
				// or, line 53
				lab9: do {
					v_6 = cursor;
					lab10: do {
						// (, line 53
						if (!(out_grouping(g_v, 97, 250))) {
							break lab10;
						}
						// gopast, line 53
						golab11: while (true) {
							lab12: do {
								if (!(in_grouping(g_v, 97, 250))) {
									break lab12;
								}
								break golab11;
							} while (false);
							if (cursor >= limit) {
								break lab10;
							}
							cursor++;
						}
						break lab9;
					} while (false);
					cursor = v_6;
					// (, line 53
					if (!(in_grouping(g_v, 97, 250))) {
						break lab0;
					}
					// next, line 53
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				} while (false);
			} while (false);
			// setmark pV, line 54
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 56
		v_8 = cursor;
		lab13: do {
			// (, line 56
			// gopast, line 57
			golab14: while (true) {
				lab15: do {
					if (!(in_grouping(g_v, 97, 250))) {
						break lab15;
					}
					break golab14;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 57
			golab16: while (true) {
				lab17: do {
					if (!(out_grouping(g_v, 97, 250))) {
						break lab17;
					}
					break golab16;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p1, line 57
			I_p1 = cursor;
			// gopast, line 58
			golab18: while (true) {
				lab19: do {
					if (!(in_grouping(g_v, 97, 250))) {
						break lab19;
					}
					break golab18;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 58
			golab20: while (true) {
				lab21: do {
					if (!(out_grouping(g_v, 97, 250))) {
						break lab21;
					}
					break golab20;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p2, line 58
			I_p2 = cursor;
		} while (false);
		cursor = v_8;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 62
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 62
				// [, line 63
				bra = cursor;
				// substring, line 63
				among_var = find_among(a_1);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 63
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 64
					// <-, line 64
					slice_from("\u00E3");
					break;
				case 2:
					// (, line 65
					// <-, line 65
					slice_from("\u00F5");
					break;
				case 3:
					// (, line 66
					// next, line 66
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 76
		// [, line 77
		ket = cursor;
		// substring, line 77
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 77
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 92
			// call R2, line 93
			if (!r_R2()) {
				return false;
			}
			// delete, line 93
			slice_del();
			break;
		case 2:
			// (, line 97
			// call R2, line 98
			if (!r_R2()) {
				return false;
			}
			// <-, line 98
			slice_from("log");
			break;
		case 3:
			// (, line 101
			// call R2, line 102
			if (!r_R2()) {
				return false;
			}
			// <-, line 102
			slice_from("u");
			break;
		case 4:
			// (, line 105
			// call R2, line 106
			if (!r_R2()) {
				return false;
			}
			// <-, line 106
			slice_from("ente");
			break;
		case 5:
			// (, line 109
			// call R1, line 110
			if (!r_R1()) {
				return false;
			}
			// delete, line 110
			slice_del();
			// try, line 111
			v_1 = limit - cursor;
			lab0: do {
				// (, line 111
				// [, line 112
				ket = cursor;
				// substring, line 112
				among_var = find_among_b(a_2);
				if (among_var == 0) {
					cursor = limit - v_1;
					break lab0;
				}
				// ], line 112
				bra = cursor;
				// call R2, line 112
				if (!r_R2()) {
					cursor = limit - v_1;
					break lab0;
				}
				// delete, line 112
				slice_del();
				switch (among_var) {
				case 0:
					cursor = limit - v_1;
					break lab0;
				case 1:
					// (, line 113
					// [, line 113
					ket = cursor;
					// literal, line 113
					if (!(eq_s_b("at"))) {
						cursor = limit - v_1;
						break lab0;
					}
					// ], line 113
					bra = cursor;
					// call R2, line 113
					if (!r_R2()) {
						cursor = limit - v_1;
						break lab0;
					}
					// delete, line 113
					slice_del();
					break;
				}
			} while (false);
			break;
		case 6:
			// (, line 121
			// call R2, line 122
			if (!r_R2()) {
				return false;
			}
			// delete, line 122
			slice_del();
			// try, line 123
			v_2 = limit - cursor;
			lab1: do {
				// (, line 123
				// [, line 124
				ket = cursor;
				// substring, line 124
				among_var = find_among_b(a_3);
				if (among_var == 0) {
					cursor = limit - v_2;
					break lab1;
				}
				// ], line 124
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_2;
					break lab1;
				case 1:
					// (, line 127
					// call R2, line 127
					if (!r_R2()) {
						cursor = limit - v_2;
						break lab1;
					}
					// delete, line 127
					slice_del();
					break;
				}
			} while (false);
			break;
		case 7:
			// (, line 133
			// call R2, line 134
			if (!r_R2()) {
				return false;
			}
			// delete, line 134
			slice_del();
			// try, line 135
			v_3 = limit - cursor;
			lab2: do {
				// (, line 135
				// [, line 136
				ket = cursor;
				// substring, line 136
				among_var = find_among_b(a_4);
				if (among_var == 0) {
					cursor = limit - v_3;
					break lab2;
				}
				// ], line 136
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_3;
					break lab2;
				case 1:
					// (, line 139
					// call R2, line 139
					if (!r_R2()) {
						cursor = limit - v_3;
						break lab2;
					}
					// delete, line 139
					slice_del();
					break;
				}
			} while (false);
			break;
		case 8:
			// (, line 145
			// call R2, line 146
			if (!r_R2()) {
				return false;
			}
			// delete, line 146
			slice_del();
			// try, line 147
			v_4 = limit - cursor;
			lab3: do {
				// (, line 147
				// [, line 148
				ket = cursor;
				// literal, line 148
				if (!(eq_s_b("at"))) {
					cursor = limit - v_4;
					break lab3;
				}
				// ], line 148
				bra = cursor;
				// call R2, line 148
				if (!r_R2()) {
					cursor = limit - v_4;
					break lab3;
				}
				// delete, line 148
				slice_del();
			} while (false);
			break;
		case 9:
			// (, line 152
			// call RV, line 153
			if (!r_RV()) {
				return false;
			}
			// literal, line 153
			if (!(eq_s_b("e"))) {
				return false;
			}
			// <-, line 154
			slice_from("ir");
			break;
		}
		return true;
	}

	private boolean r_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// setlimit, line 159
		v_1 = limit - cursor;
		// tomark, line 159
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 159
		// [, line 160
		ket = cursor;
		// substring, line 160
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 160
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_2;
			return false;
		case 1:
			// (, line 179
			// delete, line 179
			slice_del();
			break;
		}
		limit_backward = v_2;
		return true;
	}

	private boolean r_residual_suffix() {
		int among_var;
		// (, line 183
		// [, line 184
		ket = cursor;
		// substring, line 184
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			return false;
		}
		// ], line 184
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 187
			// call RV, line 187
			if (!r_RV()) {
				return false;
			}
			// delete, line 187
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_residual_form() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		// (, line 191
		// [, line 192
		ket = cursor;
		// substring, line 192
		among_var = find_among_b(a_8);
		if (among_var == 0) {
			return false;
		}
		// ], line 192
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 194
			// call RV, line 194
			if (!r_RV()) {
				return false;
			}
			// delete, line 194
			slice_del();
			// [, line 194
			ket = cursor;
			// or, line 194
			lab0: do {
				v_1 = limit - cursor;
				lab1: do {
					// (, line 194
					// literal, line 194
					if (!(eq_s_b("u"))) {
						break lab1;
					}
					// ], line 194
					bra = cursor;
					// test, line 194
					v_2 = limit - cursor;
					// literal, line 194
					if (!(eq_s_b("g"))) {
						break lab1;
					}
					cursor = limit - v_2;
					break lab0;
				} while (false);
				cursor = limit - v_1;
				// (, line 195
				// literal, line 195
				if (!(eq_s_b("i"))) {
					return false;
				}
				// ], line 195
				bra = cursor;
				// test, line 195
				v_3 = limit - cursor;
				// literal, line 195
				if (!(eq_s_b("c"))) {
					return false;
				}
				cursor = limit - v_3;
			} while (false);
			// call RV, line 195
			if (!r_RV()) {
				return false;
			}
			// delete, line 195
			slice_del();
			break;
		case 2:
			// (, line 196
			// <-, line 196
			slice_from("c");
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 201
		// do, line 202
		v_1 = cursor;
		lab0: do {
			// call prelude, line 202
			if (!r_prelude()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 203
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 203
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 204
		limit_backward = cursor;
		cursor = limit;
		// (, line 204
		// do, line 205
		v_3 = limit - cursor;
		lab2: do {
			// (, line 205
			// or, line 209
			lab3: do {
				v_4 = limit - cursor;
				lab4: do {
					// (, line 206
					// and, line 207
					v_5 = limit - cursor;
					// (, line 206
					// or, line 206
					lab5: do {
						v_6 = limit - cursor;
						lab6: do {
							// call standard_suffix, line 206
							if (!r_standard_suffix()) {
								break lab6;
							}
							break lab5;
						} while (false);
						cursor = limit - v_6;
						// call verb_suffix, line 206
						if (!r_verb_suffix()) {
							break lab4;
						}
					} while (false);
					cursor = limit - v_5;
					// do, line 207
					v_7 = limit - cursor;
					lab7: do {
						// (, line 207
						// [, line 207
						ket = cursor;
						// literal, line 207
						if (!(eq_s_b("i"))) {
							break lab7;
						}
						// ], line 207
						bra = cursor;
						// test, line 207
						v_8 = limit - cursor;
						// literal, line 207
						if (!(eq_s_b("c"))) {
							break lab7;
						}
						cursor = limit - v_8;
						// call RV, line 207
						if (!r_RV()) {
							break lab7;
						}
						// delete, line 207
						slice_del();
					} while (false);
					cursor = limit - v_7;
					break lab3;
				} while (false);
				cursor = limit - v_4;
				// call residual_suffix, line 209
				if (!r_residual_suffix()) {
					break lab2;
				}
			} while (false);
		} while (false);
		cursor = limit - v_3;
		// do, line 211
		v_9 = limit - cursor;
		lab8: do {
			// call residual_form, line 211
			if (!r_residual_form()) {
				break lab8;
			}
		} while (false);
		cursor = limit - v_9;
		cursor = limit_backward; // do, line 213
		v_10 = cursor;
		lab9: do {
			// call postlude, line 213
			if (!r_postlude()) {
				break lab9;
			}
		} while (false);
		cursor = v_10;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class romanianStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 3), new Among("I", 0, 1), new Among("U", 0, 2) };

	private final static Among a_1[] = { new Among("ea", -1, 3), new Among("a\u0163ia", -1, 7), new Among("aua", -1, 2),
			new Among("iua", -1, 4), new Among("a\u0163ie", -1, 7), new Among("ele", -1, 3), new Among("ile", -1, 5),
			new Among("iile", 6, 4), new Among("iei", -1, 4), new Among("atei", -1, 6), new Among("ii", -1, 4),
			new Among("ului", -1, 1), new Among("ul", -1, 1), new Among("elor", -1, 3), new Among("ilor", -1, 4),
			new Among("iilor", 14, 4) };

	private final static Among a_2[] = { new Among("icala", -1, 4), new Among("iciva", -1, 4),
			new Among("ativa", -1, 5), new Among("itiva", -1, 6), new Among("icale", -1, 4),
			new Among("a\u0163iune", -1, 5), new Among("i\u0163iune", -1, 6), new Among("atoare", -1, 5),
			new Among("itoare", -1, 6), new Among("\u0103toare", -1, 5), new Among("icitate", -1, 4),
			new Among("abilitate", -1, 1), new Among("ibilitate", -1, 2), new Among("ivitate", -1, 3),
			new Among("icive", -1, 4), new Among("ative", -1, 5), new Among("itive", -1, 6), new Among("icali", -1, 4),
			new Among("atori", -1, 5), new Among("icatori", 18, 4), new Among("itori", -1, 6),
			new Among("\u0103tori", -1, 5), new Among("icitati", -1, 4), new Among("abilitati", -1, 1),
			new Among("ivitati", -1, 3), new Among("icivi", -1, 4), new Among("ativi", -1, 5),
			new Among("itivi", -1, 6), new Among("icit\u0103i", -1, 4), new Among("abilit\u0103i", -1, 1),
			new Among("ivit\u0103i", -1, 3), new Among("icit\u0103\u0163i", -1, 4),
			new Among("abilit\u0103\u0163i", -1, 1), new Among("ivit\u0103\u0163i", -1, 3), new Among("ical", -1, 4),
			new Among("ator", -1, 5), new Among("icator", 35, 4), new Among("itor", -1, 6),
			new Among("\u0103tor", -1, 5), new Among("iciv", -1, 4), new Among("ativ", -1, 5), new Among("itiv", -1, 6),
			new Among("ical\u0103", -1, 4), new Among("iciv\u0103", -1, 4), new Among("ativ\u0103", -1, 5),
			new Among("itiv\u0103", -1, 6) };

	private final static Among a_3[] = { new Among("ica", -1, 1), new Among("abila", -1, 1), new Among("ibila", -1, 1),
			new Among("oasa", -1, 1), new Among("ata", -1, 1), new Among("ita", -1, 1), new Among("anta", -1, 1),
			new Among("ista", -1, 3), new Among("uta", -1, 1), new Among("iva", -1, 1), new Among("ic", -1, 1),
			new Among("ice", -1, 1), new Among("abile", -1, 1), new Among("ibile", -1, 1), new Among("isme", -1, 3),
			new Among("iune", -1, 2), new Among("oase", -1, 1), new Among("ate", -1, 1), new Among("itate", 17, 1),
			new Among("ite", -1, 1), new Among("ante", -1, 1), new Among("iste", -1, 3), new Among("ute", -1, 1),
			new Among("ive", -1, 1), new Among("ici", -1, 1), new Among("abili", -1, 1), new Among("ibili", -1, 1),
			new Among("iuni", -1, 2), new Among("atori", -1, 1), new Among("osi", -1, 1), new Among("ati", -1, 1),
			new Among("itati", 30, 1), new Among("iti", -1, 1), new Among("anti", -1, 1), new Among("isti", -1, 3),
			new Among("uti", -1, 1), new Among("i\u015Fti", -1, 3), new Among("ivi", -1, 1),
			new Among("it\u0103i", -1, 1), new Among("o\u015Fi", -1, 1), new Among("it\u0103\u0163i", -1, 1),
			new Among("abil", -1, 1), new Among("ibil", -1, 1), new Among("ism", -1, 3), new Among("ator", -1, 1),
			new Among("os", -1, 1), new Among("at", -1, 1), new Among("it", -1, 1), new Among("ant", -1, 1),
			new Among("ist", -1, 3), new Among("ut", -1, 1), new Among("iv", -1, 1), new Among("ic\u0103", -1, 1),
			new Among("abil\u0103", -1, 1), new Among("ibil\u0103", -1, 1), new Among("oas\u0103", -1, 1),
			new Among("at\u0103", -1, 1), new Among("it\u0103", -1, 1), new Among("ant\u0103", -1, 1),
			new Among("ist\u0103", -1, 3), new Among("ut\u0103", -1, 1), new Among("iv\u0103", -1, 1) };

	private final static Among a_4[] = { new Among("ea", -1, 1), new Among("ia", -1, 1), new Among("esc", -1, 1),
			new Among("\u0103sc", -1, 1), new Among("ind", -1, 1), new Among("\u00E2nd", -1, 1),
			new Among("are", -1, 1), new Among("ere", -1, 1), new Among("ire", -1, 1), new Among("\u00E2re", -1, 1),
			new Among("se", -1, 2), new Among("ase", 10, 1), new Among("sese", 10, 2), new Among("ise", 10, 1),
			new Among("use", 10, 1), new Among("\u00E2se", 10, 1), new Among("e\u015Fte", -1, 1),
			new Among("\u0103\u015Fte", -1, 1), new Among("eze", -1, 1), new Among("ai", -1, 1),
			new Among("eai", 19, 1), new Among("iai", 19, 1), new Among("sei", -1, 2), new Among("e\u015Fti", -1, 1),
			new Among("\u0103\u015Fti", -1, 1), new Among("ui", -1, 1), new Among("ezi", -1, 1),
			new Among("\u00E2i", -1, 1), new Among("a\u015Fi", -1, 1), new Among("se\u015Fi", -1, 2),
			new Among("ase\u015Fi", 29, 1), new Among("sese\u015Fi", 29, 2), new Among("ise\u015Fi", 29, 1),
			new Among("use\u015Fi", 29, 1), new Among("\u00E2se\u015Fi", 29, 1), new Among("i\u015Fi", -1, 1),
			new Among("u\u015Fi", -1, 1), new Among("\u00E2\u015Fi", -1, 1), new Among("a\u0163i", -1, 2),
			new Among("ea\u0163i", 38, 1), new Among("ia\u0163i", 38, 1), new Among("e\u0163i", -1, 2),
			new Among("i\u0163i", -1, 2), new Among("\u00E2\u0163i", -1, 2), new Among("ar\u0103\u0163i", -1, 1),
			new Among("ser\u0103\u0163i", -1, 2), new Among("aser\u0103\u0163i", 45, 1),
			new Among("seser\u0103\u0163i", 45, 2), new Among("iser\u0103\u0163i", 45, 1),
			new Among("user\u0103\u0163i", 45, 1), new Among("\u00E2ser\u0103\u0163i", 45, 1),
			new Among("ir\u0103\u0163i", -1, 1), new Among("ur\u0103\u0163i", -1, 1),
			new Among("\u00E2r\u0103\u0163i", -1, 1), new Among("am", -1, 1), new Among("eam", 54, 1),
			new Among("iam", 54, 1), new Among("em", -1, 2), new Among("asem", 57, 1), new Among("sesem", 57, 2),
			new Among("isem", 57, 1), new Among("usem", 57, 1), new Among("\u00E2sem", 57, 1), new Among("im", -1, 2),
			new Among("\u00E2m", -1, 2), new Among("\u0103m", -1, 2), new Among("ar\u0103m", 65, 1),
			new Among("ser\u0103m", 65, 2), new Among("aser\u0103m", 67, 1), new Among("seser\u0103m", 67, 2),
			new Among("iser\u0103m", 67, 1), new Among("user\u0103m", 67, 1), new Among("\u00E2ser\u0103m", 67, 1),
			new Among("ir\u0103m", 65, 1), new Among("ur\u0103m", 65, 1), new Among("\u00E2r\u0103m", 65, 1),
			new Among("au", -1, 1), new Among("eau", 76, 1), new Among("iau", 76, 1), new Among("indu", -1, 1),
			new Among("\u00E2ndu", -1, 1), new Among("ez", -1, 1), new Among("easc\u0103", -1, 1),
			new Among("ar\u0103", -1, 1), new Among("ser\u0103", -1, 2), new Among("aser\u0103", 84, 1),
			new Among("seser\u0103", 84, 2), new Among("iser\u0103", 84, 1), new Among("user\u0103", 84, 1),
			new Among("\u00E2ser\u0103", 84, 1), new Among("ir\u0103", -1, 1), new Among("ur\u0103", -1, 1),
			new Among("\u00E2r\u0103", -1, 1), new Among("eaz\u0103", -1, 1) };

	private final static Among a_5[] = { new Among("a", -1, 1), new Among("e", -1, 1), new Among("ie", 1, 1),
			new Among("i", -1, 1), new Among("\u0103", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 32, 0, 0, 4 };

	private boolean B_standard_suffix_removed;
	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_prelude() {
		int v_1;
		int v_2;
		int v_3;
		// (, line 31
		// repeat, line 32
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// goto, line 32
				golab2: while (true) {
					v_2 = cursor;
					lab3: do {
						// (, line 32
						if (!(in_grouping(g_v, 97, 259))) {
							break lab3;
						}
						// [, line 33
						bra = cursor;
						// or, line 33
						lab4: do {
							v_3 = cursor;
							lab5: do {
								// (, line 33
								// literal, line 33
								if (!(eq_s("u"))) {
									break lab5;
								}
								// ], line 33
								ket = cursor;
								if (!(in_grouping(g_v, 97, 259))) {
									break lab5;
								}
								// <-, line 33
								slice_from("U");
								break lab4;
							} while (false);
							cursor = v_3;
							// (, line 34
							// literal, line 34
							if (!(eq_s("i"))) {
								break lab3;
							}
							// ], line 34
							ket = cursor;
							if (!(in_grouping(g_v, 97, 259))) {
								break lab3;
							}
							// <-, line 34
							slice_from("I");
						} while (false);
						cursor = v_2;
						break golab2;
					} while (false);
					cursor = v_2;
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_3;
		int v_6;
		int v_8;
		// (, line 38
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 44
		v_1 = cursor;
		lab0: do {
			// (, line 44
			// or, line 46
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// (, line 45
					if (!(in_grouping(g_v, 97, 259))) {
						break lab2;
					}
					// or, line 45
					lab3: do {
						v_3 = cursor;
						lab4: do {
							// (, line 45
							if (!(out_grouping(g_v, 97, 259))) {
								break lab4;
							}
							// gopast, line 45
							golab5: while (true) {
								lab6: do {
									if (!(in_grouping(g_v, 97, 259))) {
										break lab6;
									}
									break golab5;
								} while (false);
								if (cursor >= limit) {
									break lab4;
								}
								cursor++;
							}
							break lab3;
						} while (false);
						cursor = v_3;
						// (, line 45
						if (!(in_grouping(g_v, 97, 259))) {
							break lab2;
						}
						// gopast, line 45
						golab7: while (true) {
							lab8: do {
								if (!(out_grouping(g_v, 97, 259))) {
									break lab8;
								}
								break golab7;
							} while (false);
							if (cursor >= limit) {
								break lab2;
							}
							cursor++;
						}
					} while (false);
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 47
				if (!(out_grouping(g_v, 97, 259))) {
					break lab0;
				}
				// or, line 47
				lab9: do {
					v_6 = cursor;
					lab10: do {
						// (, line 47
						if (!(out_grouping(g_v, 97, 259))) {
							break lab10;
						}
						// gopast, line 47
						golab11: while (true) {
							lab12: do {
								if (!(in_grouping(g_v, 97, 259))) {
									break lab12;
								}
								break golab11;
							} while (false);
							if (cursor >= limit) {
								break lab10;
							}
							cursor++;
						}
						break lab9;
					} while (false);
					cursor = v_6;
					// (, line 47
					if (!(in_grouping(g_v, 97, 259))) {
						break lab0;
					}
					// next, line 47
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				} while (false);
			} while (false);
			// setmark pV, line 48
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 50
		v_8 = cursor;
		lab13: do {
			// (, line 50
			// gopast, line 51
			golab14: while (true) {
				lab15: do {
					if (!(in_grouping(g_v, 97, 259))) {
						break lab15;
					}
					break golab14;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 51
			golab16: while (true) {
				lab17: do {
					if (!(out_grouping(g_v, 97, 259))) {
						break lab17;
					}
					break golab16;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p1, line 51
			I_p1 = cursor;
			// gopast, line 52
			golab18: while (true) {
				lab19: do {
					if (!(in_grouping(g_v, 97, 259))) {
						break lab19;
					}
					break golab18;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 52
			golab20: while (true) {
				lab21: do {
					if (!(out_grouping(g_v, 97, 259))) {
						break lab21;
					}
					break golab20;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p2, line 52
			I_p2 = cursor;
		} while (false);
		cursor = v_8;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 56
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 56
				// [, line 58
				bra = cursor;
				// substring, line 58
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 58
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 59
					// <-, line 59
					slice_from("i");
					break;
				case 2:
					// (, line 60
					// <-, line 60
					slice_from("u");
					break;
				case 3:
					// (, line 61
					// next, line 61
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_step_0() {
		int among_var;
		int v_1;
		// (, line 72
		// [, line 73
		ket = cursor;
		// substring, line 73
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 73
		bra = cursor;
		// call R1, line 73
		if (!r_R1()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 75
			// delete, line 75
			slice_del();
			break;
		case 2:
			// (, line 77
			// <-, line 77
			slice_from("a");
			break;
		case 3:
			// (, line 79
			// <-, line 79
			slice_from("e");
			break;
		case 4:
			// (, line 81
			// <-, line 81
			slice_from("i");
			break;
		case 5:
		// (, line 83
		// not, line 83
		{
			v_1 = limit - cursor;
			lab0: do {
				// literal, line 83
				if (!(eq_s_b("ab"))) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = limit - v_1;
		}
			// <-, line 83
			slice_from("i");
			break;
		case 6:
			// (, line 85
			// <-, line 85
			slice_from("at");
			break;
		case 7:
			// (, line 87
			// <-, line 87
			slice_from("a\u0163i");
			break;
		}
		return true;
	}

	private boolean r_combo_suffix() {
		int among_var;
		int v_1;
		// test, line 91
		v_1 = limit - cursor;
		// (, line 91
		// [, line 92
		ket = cursor;
		// substring, line 92
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// ], line 92
		bra = cursor;
		// call R1, line 92
		if (!r_R1()) {
			return false;
		}
		// (, line 92
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 100
			// <-, line 101
			slice_from("abil");
			break;
		case 2:
			// (, line 103
			// <-, line 104
			slice_from("ibil");
			break;
		case 3:
			// (, line 106
			// <-, line 107
			slice_from("iv");
			break;
		case 4:
			// (, line 112
			// <-, line 113
			slice_from("ic");
			break;
		case 5:
			// (, line 117
			// <-, line 118
			slice_from("at");
			break;
		case 6:
			// (, line 121
			// <-, line 122
			slice_from("it");
			break;
		}
		// set standard_suffix_removed, line 125
		B_standard_suffix_removed = true;
		cursor = limit - v_1;
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		// (, line 129
		// unset standard_suffix_removed, line 130
		B_standard_suffix_removed = false;
		// repeat, line 131
		replab0: while (true) {
			v_1 = limit - cursor;
			lab1: do {
				// call combo_suffix, line 131
				if (!r_combo_suffix()) {
					break lab1;
				}
				continue replab0;
			} while (false);
			cursor = limit - v_1;
			break replab0;
		}
		// [, line 132
		ket = cursor;
		// substring, line 132
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 132
		bra = cursor;
		// call R2, line 132
		if (!r_R2()) {
			return false;
		}
		// (, line 132
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 148
			// delete, line 149
			slice_del();
			break;
		case 2:
			// (, line 151
			// literal, line 152
			if (!(eq_s_b("\u0163"))) {
				return false;
			}
			// ], line 152
			bra = cursor;
			// <-, line 152
			slice_from("t");
			break;
		case 3:
			// (, line 155
			// <-, line 156
			slice_from("ist");
			break;
		}
		// set standard_suffix_removed, line 160
		B_standard_suffix_removed = true;
		return true;
	}

	private boolean r_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		// setlimit, line 164
		v_1 = limit - cursor;
		// tomark, line 164
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 164
		// [, line 165
		ket = cursor;
		// substring, line 165
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 165
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_2;
			return false;
		case 1:
			// (, line 200
			// or, line 200
			lab0: do {
				v_3 = limit - cursor;
				lab1: do {
					if (!(out_grouping_b(g_v, 97, 259))) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_3;
				// literal, line 200
				if (!(eq_s_b("u"))) {
					limit_backward = v_2;
					return false;
				}
			} while (false);
			// delete, line 200
			slice_del();
			break;
		case 2:
			// (, line 214
			// delete, line 214
			slice_del();
			break;
		}
		limit_backward = v_2;
		return true;
	}

	private boolean r_vowel_suffix() {
		int among_var;
		// (, line 218
		// [, line 219
		ket = cursor;
		// substring, line 219
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 219
		bra = cursor;
		// call RV, line 219
		if (!r_RV()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 220
			// delete, line 220
			slice_del();
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		// (, line 225
		// do, line 226
		v_1 = cursor;
		lab0: do {
			// call prelude, line 226
			if (!r_prelude()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// do, line 227
		v_2 = cursor;
		lab1: do {
			// call mark_regions, line 227
			if (!r_mark_regions()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// backwards, line 228
		limit_backward = cursor;
		cursor = limit;
		// (, line 228
		// do, line 229
		v_3 = limit - cursor;
		lab2: do {
			// call step_0, line 229
			if (!r_step_0()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 230
		v_4 = limit - cursor;
		lab3: do {
			// call standard_suffix, line 230
			if (!r_standard_suffix()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		// do, line 231
		v_5 = limit - cursor;
		lab4: do {
			// (, line 231
			// or, line 231
			lab5: do {
				v_6 = limit - cursor;
				lab6: do {
					// Boolean test standard_suffix_removed, line 231
					if (!(B_standard_suffix_removed)) {
						break lab6;
					}
					break lab5;
				} while (false);
				cursor = limit - v_6;
				// call verb_suffix, line 231
				if (!r_verb_suffix()) {
					break lab4;
				}
			} while (false);
		} while (false);
		cursor = limit - v_5;
		// do, line 232
		v_7 = limit - cursor;
		lab7: do {
			// call vowel_suffix, line 232
			if (!r_vowel_suffix()) {
				break lab7;
			}
		} while (false);
		cursor = limit - v_7;
		cursor = limit_backward; // do, line 234
		v_8 = cursor;
		lab8: do {
			// call postlude, line 234
			if (!r_postlude()) {
				break lab8;
			}
		} while (false);
		cursor = v_8;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class russianStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("\u0432", -1, 1), new Among("\u0438\u0432", 0, 2),
			new Among("\u044B\u0432", 0, 2), new Among("\u0432\u0448\u0438", -1, 1),
			new Among("\u0438\u0432\u0448\u0438", 3, 2), new Among("\u044B\u0432\u0448\u0438", 3, 2),
			new Among("\u0432\u0448\u0438\u0441\u044C", -1, 1), new Among("\u0438\u0432\u0448\u0438\u0441\u044C", 6, 2),
			new Among("\u044B\u0432\u0448\u0438\u0441\u044C", 6, 2) };

	private final static Among a_1[] = { new Among("\u0435\u0435", -1, 1), new Among("\u0438\u0435", -1, 1),
			new Among("\u043E\u0435", -1, 1), new Among("\u044B\u0435", -1, 1), new Among("\u0438\u043C\u0438", -1, 1),
			new Among("\u044B\u043C\u0438", -1, 1), new Among("\u0435\u0439", -1, 1), new Among("\u0438\u0439", -1, 1),
			new Among("\u043E\u0439", -1, 1), new Among("\u044B\u0439", -1, 1), new Among("\u0435\u043C", -1, 1),
			new Among("\u0438\u043C", -1, 1), new Among("\u043E\u043C", -1, 1), new Among("\u044B\u043C", -1, 1),
			new Among("\u0435\u0433\u043E", -1, 1), new Among("\u043E\u0433\u043E", -1, 1),
			new Among("\u0435\u043C\u0443", -1, 1), new Among("\u043E\u043C\u0443", -1, 1),
			new Among("\u0438\u0445", -1, 1), new Among("\u044B\u0445", -1, 1), new Among("\u0435\u044E", -1, 1),
			new Among("\u043E\u044E", -1, 1), new Among("\u0443\u044E", -1, 1), new Among("\u044E\u044E", -1, 1),
			new Among("\u0430\u044F", -1, 1), new Among("\u044F\u044F", -1, 1) };

	private final static Among a_2[] = { new Among("\u0435\u043C", -1, 1), new Among("\u043D\u043D", -1, 1),
			new Among("\u0432\u0448", -1, 1), new Among("\u0438\u0432\u0448", 2, 2),
			new Among("\u044B\u0432\u0448", 2, 2), new Among("\u0449", -1, 1), new Among("\u044E\u0449", 5, 1),
			new Among("\u0443\u044E\u0449", 6, 2) };

	private final static Among a_3[] = { new Among("\u0441\u044C", -1, 1), new Among("\u0441\u044F", -1, 1) };

	private final static Among a_4[] = { new Among("\u043B\u0430", -1, 1), new Among("\u0438\u043B\u0430", 0, 2),
			new Among("\u044B\u043B\u0430", 0, 2), new Among("\u043D\u0430", -1, 1),
			new Among("\u0435\u043D\u0430", 3, 2), new Among("\u0435\u0442\u0435", -1, 1),
			new Among("\u0438\u0442\u0435", -1, 2), new Among("\u0439\u0442\u0435", -1, 1),
			new Among("\u0435\u0439\u0442\u0435", 7, 2), new Among("\u0443\u0439\u0442\u0435", 7, 2),
			new Among("\u043B\u0438", -1, 1), new Among("\u0438\u043B\u0438", 10, 2),
			new Among("\u044B\u043B\u0438", 10, 2), new Among("\u0439", -1, 1), new Among("\u0435\u0439", 13, 2),
			new Among("\u0443\u0439", 13, 2), new Among("\u043B", -1, 1), new Among("\u0438\u043B", 16, 2),
			new Among("\u044B\u043B", 16, 2), new Among("\u0435\u043C", -1, 1), new Among("\u0438\u043C", -1, 2),
			new Among("\u044B\u043C", -1, 2), new Among("\u043D", -1, 1), new Among("\u0435\u043D", 22, 2),
			new Among("\u043B\u043E", -1, 1), new Among("\u0438\u043B\u043E", 24, 2),
			new Among("\u044B\u043B\u043E", 24, 2), new Among("\u043D\u043E", -1, 1),
			new Among("\u0435\u043D\u043E", 27, 2), new Among("\u043D\u043D\u043E", 27, 1),
			new Among("\u0435\u0442", -1, 1), new Among("\u0443\u0435\u0442", 30, 2), new Among("\u0438\u0442", -1, 2),
			new Among("\u044B\u0442", -1, 2), new Among("\u044E\u0442", -1, 1), new Among("\u0443\u044E\u0442", 34, 2),
			new Among("\u044F\u0442", -1, 2), new Among("\u043D\u044B", -1, 1), new Among("\u0435\u043D\u044B", 37, 2),
			new Among("\u0442\u044C", -1, 1), new Among("\u0438\u0442\u044C", 39, 2),
			new Among("\u044B\u0442\u044C", 39, 2), new Among("\u0435\u0448\u044C", -1, 1),
			new Among("\u0438\u0448\u044C", -1, 2), new Among("\u044E", -1, 2), new Among("\u0443\u044E", 44, 2) };

	private final static Among a_5[] = { new Among("\u0430", -1, 1), new Among("\u0435\u0432", -1, 1),
			new Among("\u043E\u0432", -1, 1), new Among("\u0435", -1, 1), new Among("\u0438\u0435", 3, 1),
			new Among("\u044C\u0435", 3, 1), new Among("\u0438", -1, 1), new Among("\u0435\u0438", 6, 1),
			new Among("\u0438\u0438", 6, 1), new Among("\u0430\u043C\u0438", 6, 1),
			new Among("\u044F\u043C\u0438", 6, 1), new Among("\u0438\u044F\u043C\u0438", 10, 1),
			new Among("\u0439", -1, 1), new Among("\u0435\u0439", 12, 1), new Among("\u0438\u0435\u0439", 13, 1),
			new Among("\u0438\u0439", 12, 1), new Among("\u043E\u0439", 12, 1), new Among("\u0430\u043C", -1, 1),
			new Among("\u0435\u043C", -1, 1), new Among("\u0438\u0435\u043C", 18, 1), new Among("\u043E\u043C", -1, 1),
			new Among("\u044F\u043C", -1, 1), new Among("\u0438\u044F\u043C", 21, 1), new Among("\u043E", -1, 1),
			new Among("\u0443", -1, 1), new Among("\u0430\u0445", -1, 1), new Among("\u044F\u0445", -1, 1),
			new Among("\u0438\u044F\u0445", 26, 1), new Among("\u044B", -1, 1), new Among("\u044C", -1, 1),
			new Among("\u044E", -1, 1), new Among("\u0438\u044E", 30, 1), new Among("\u044C\u044E", 30, 1),
			new Among("\u044F", -1, 1), new Among("\u0438\u044F", 33, 1), new Among("\u044C\u044F", 33, 1) };

	private final static Among a_6[] = { new Among("\u043E\u0441\u0442", -1, 1),
			new Among("\u043E\u0441\u0442\u044C", -1, 1) };

	private final static Among a_7[] = { new Among("\u0435\u0439\u0448\u0435", -1, 1), new Among("\u043D", -1, 2),
			new Among("\u0435\u0439\u0448", -1, 1), new Among("\u044C", -1, 3) };

	private static final char g_v[] = { 33, 65, 8, 232 };

	private int I_p2;
	private int I_pV;

	private boolean r_mark_regions() {
		int v_1;
		// (, line 57
		I_pV = limit;
		I_p2 = limit;
		// do, line 61
		v_1 = cursor;
		lab0: do {
			// (, line 61
			// gopast, line 62
			golab1: while (true) {
				lab2: do {
					if (!(in_grouping(g_v, 1072, 1103))) {
						break lab2;
					}
					break golab1;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark pV, line 62
			I_pV = cursor;
			// gopast, line 62
			golab3: while (true) {
				lab4: do {
					if (!(out_grouping(g_v, 1072, 1103))) {
						break lab4;
					}
					break golab3;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 63
			golab5: while (true) {
				lab6: do {
					if (!(in_grouping(g_v, 1072, 1103))) {
						break lab6;
					}
					break golab5;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// gopast, line 63
			golab7: while (true) {
				lab8: do {
					if (!(out_grouping(g_v, 1072, 1103))) {
						break lab8;
					}
					break golab7;
				} while (false);
				if (cursor >= limit) {
					break lab0;
				}
				cursor++;
			}
			// setmark p2, line 63
			I_p2 = cursor;
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_perfective_gerund() {
		int among_var;
		int v_1;
		// (, line 71
		// [, line 72
		ket = cursor;
		// substring, line 72
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			return false;
		}
		// ], line 72
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 76
			// or, line 76
			lab0: do {
				v_1 = limit - cursor;
				lab1: do {
					// literal, line 76
					if (!(eq_s_b("\u0430"))) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_1;
				// literal, line 76
				if (!(eq_s_b("\u044F"))) {
					return false;
				}
			} while (false);
			// delete, line 76
			slice_del();
			break;
		case 2:
			// (, line 83
			// delete, line 83
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_adjective() {
		int among_var;
		// (, line 87
		// [, line 88
		ket = cursor;
		// substring, line 88
		among_var = find_among_b(a_1);
		if (among_var == 0) {
			return false;
		}
		// ], line 88
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 97
			// delete, line 97
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_adjectival() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 101
		// call adjective, line 102
		if (!r_adjective()) {
			return false;
		}
		// try, line 109
		v_1 = limit - cursor;
		lab0: do {
			// (, line 109
			// [, line 110
			ket = cursor;
			// substring, line 110
			among_var = find_among_b(a_2);
			if (among_var == 0) {
				cursor = limit - v_1;
				break lab0;
			}
			// ], line 110
			bra = cursor;
			switch (among_var) {
			case 0:
				cursor = limit - v_1;
				break lab0;
			case 1:
				// (, line 115
				// or, line 115
				lab1: do {
					v_2 = limit - cursor;
					lab2: do {
						// literal, line 115
						if (!(eq_s_b("\u0430"))) {
							break lab2;
						}
						break lab1;
					} while (false);
					cursor = limit - v_2;
					// literal, line 115
					if (!(eq_s_b("\u044F"))) {
						cursor = limit - v_1;
						break lab0;
					}
				} while (false);
				// delete, line 115
				slice_del();
				break;
			case 2:
				// (, line 122
				// delete, line 122
				slice_del();
				break;
			}
		} while (false);
		return true;
	}

	private boolean r_reflexive() {
		int among_var;
		// (, line 128
		// [, line 129
		ket = cursor;
		// substring, line 129
		among_var = find_among_b(a_3);
		if (among_var == 0) {
			return false;
		}
		// ], line 129
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 132
			// delete, line 132
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_verb() {
		int among_var;
		int v_1;
		// (, line 136
		// [, line 137
		ket = cursor;
		// substring, line 137
		among_var = find_among_b(a_4);
		if (among_var == 0) {
			return false;
		}
		// ], line 137
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 143
			// or, line 143
			lab0: do {
				v_1 = limit - cursor;
				lab1: do {
					// literal, line 143
					if (!(eq_s_b("\u0430"))) {
						break lab1;
					}
					break lab0;
				} while (false);
				cursor = limit - v_1;
				// literal, line 143
				if (!(eq_s_b("\u044F"))) {
					return false;
				}
			} while (false);
			// delete, line 143
			slice_del();
			break;
		case 2:
			// (, line 151
			// delete, line 151
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_noun() {
		int among_var;
		// (, line 159
		// [, line 160
		ket = cursor;
		// substring, line 160
		among_var = find_among_b(a_5);
		if (among_var == 0) {
			return false;
		}
		// ], line 160
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 167
			// delete, line 167
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_derivational() {
		int among_var;
		// (, line 175
		// [, line 176
		ket = cursor;
		// substring, line 176
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 176
		bra = cursor;
		// call R2, line 176
		if (!r_R2()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 179
			// delete, line 179
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_tidy_up() {
		int among_var;
		// (, line 183
		// [, line 184
		ket = cursor;
		// substring, line 184
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			return false;
		}
		// ], line 184
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 188
			// delete, line 188
			slice_del();
			// [, line 189
			ket = cursor;
			// literal, line 189
			if (!(eq_s_b("\u043D"))) {
				return false;
			}
			// ], line 189
			bra = cursor;
			// literal, line 189
			if (!(eq_s_b("\u043D"))) {
				return false;
			}
			// delete, line 189
			slice_del();
			break;
		case 2:
			// (, line 192
			// literal, line 192
			if (!(eq_s_b("\u043D"))) {
				return false;
			}
			// delete, line 192
			slice_del();
			break;
		case 3:
			// (, line 194
			// delete, line 194
			slice_del();
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 199
		// do, line 201
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 201
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 202
		limit_backward = cursor;
		cursor = limit;
		// setlimit, line 202
		v_2 = limit - cursor;
		// tomark, line 202
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_3 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_2;
		// (, line 202
		// do, line 203
		v_4 = limit - cursor;
		lab1: do {
			// (, line 203
			// or, line 204
			lab2: do {
				v_5 = limit - cursor;
				lab3: do {
					// call perfective_gerund, line 204
					if (!r_perfective_gerund()) {
						break lab3;
					}
					break lab2;
				} while (false);
				cursor = limit - v_5;
				// (, line 205
				// try, line 205
				v_6 = limit - cursor;
				lab4: do {
					// call reflexive, line 205
					if (!r_reflexive()) {
						cursor = limit - v_6;
						break lab4;
					}
				} while (false);
				// or, line 206
				lab5: do {
					v_7 = limit - cursor;
					lab6: do {
						// call adjectival, line 206
						if (!r_adjectival()) {
							break lab6;
						}
						break lab5;
					} while (false);
					cursor = limit - v_7;
					lab7: do {
						// call verb, line 206
						if (!r_verb()) {
							break lab7;
						}
						break lab5;
					} while (false);
					cursor = limit - v_7;
					// call noun, line 206
					if (!r_noun()) {
						break lab1;
					}
				} while (false);
			} while (false);
		} while (false);
		cursor = limit - v_4;
		// try, line 209
		v_8 = limit - cursor;
		lab8: do {
			// (, line 209
			// [, line 209
			ket = cursor;
			// literal, line 209
			if (!(eq_s_b("\u0438"))) {
				cursor = limit - v_8;
				break lab8;
			}
			// ], line 209
			bra = cursor;
			// delete, line 209
			slice_del();
		} while (false);
		// do, line 212
		v_9 = limit - cursor;
		lab9: do {
			// call derivational, line 212
			if (!r_derivational()) {
				break lab9;
			}
		} while (false);
		cursor = limit - v_9;
		// do, line 213
		v_10 = limit - cursor;
		lab10: do {
			// call tidy_up, line 213
			if (!r_tidy_up()) {
				break lab10;
			}
		} while (false);
		cursor = limit - v_10;
		limit_backward = v_3;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class sloveneStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("anski", -1, 1), new Among("evski", -1, 1),
			new Among("ovski", -1, 1) };

	private final static Among a_1[] = { new Among("stvo", -1, 1), new Among("\u0161tvo", -1, 1) };

	private final static Among a_2[] = { new Among("ega", -1, 1), new Among("ija", -1, 1), new Among("ila", -1, 1),
			new Among("ema", -1, 1), new Among("vna", -1, 1), new Among("ite", -1, 1), new Among("ste", -1, 1),
			new Among("\u0161\u010De", -1, 1), new Among("ski", -1, 1), new Among("\u0161ki", -1, 1),
			new Among("iti", -1, 1), new Among("ovi", -1, 1), new Among("\u010Dek", -1, 1), new Among("ovm", -1, 1),
			new Among("\u010Dan", -1, 1), new Among("len", -1, 1), new Among("ven", -1, 1),
			new Among("\u0161en", -1, 1), new Among("ejo", -1, 1), new Among("ijo", -1, 1), new Among("ast", -1, 1),
			new Among("ost", -1, 1) };

	private final static Among a_3[] = { new Among("ja", -1, 1), new Among("ka", -1, 1), new Among("ma", -1, 1),
			new Among("ec", -1, 1), new Among("je", -1, 1), new Among("eg", -1, 1), new Among("eh", -1, 1),
			new Among("ih", -1, 1), new Among("mi", -1, 1), new Among("ti", -1, 1), new Among("ij", -1, 1),
			new Among("al", -1, 1), new Among("il", -1, 1), new Among("em", -1, 1), new Among("om", -1, 1),
			new Among("an", -1, 1), new Among("en", -1, 1), new Among("in", -1, 1), new Among("do", -1, 1),
			new Among("jo", -1, 1), new Among("ir", -1, 1), new Among("at", -1, 1), new Among("ev", -1, 1),
			new Among("iv", -1, 1), new Among("ov", -1, 1), new Among("o\u010D", -1, 1) };

	private final static Among a_4[] = { new Among("a", -1, 1), new Among("c", -1, 1), new Among("e", -1, 1),
			new Among("i", -1, 1), new Among("m", -1, 1), new Among("o", -1, 1), new Among("u", -1, 1),
			new Among("\u0161", -1, 1) };

	private final static Among a_5[] = { new Among("a", -1, 1), new Among("e", -1, 1), new Among("i", -1, 1),
			new Among("o", -1, 1), new Among("u", -1, 1) };

	private static final char g_soglasniki[] = { 119, 95, 23, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 0, 0, 0, 16 };

	private int I_p1;

	@Override
	public boolean stem() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 27
		I_p1 = (current.length());
		// backwards, line 30
		limit_backward = cursor;
		cursor = limit;
		// (, line 30
		// do, line 31
		v_1 = limit - cursor;
		lab0: do {
			// loop, line 31
			for (v_2 = 4; v_2 > 0; v_2--) {
				// (, line 31
				// try, line 32
				v_3 = limit - cursor;
				lab1: do {
					// (, line 32
					if (!(I_p1 > 8)) {
						cursor = limit - v_3;
						break lab1;
					}
					// [, line 33
					ket = cursor;
					// substring, line 33
					among_var = find_among_b(a_0);
					if (among_var == 0) {
						cursor = limit - v_3;
						break lab1;
					}
					// ], line 33
					bra = cursor;
					switch (among_var) {
					case 0:
						cursor = limit - v_3;
						break lab1;
					case 1:
						// (, line 33
						// delete, line 33
						slice_del();
						break;
					}
				} while (false);
				// try, line 35
				v_4 = limit - cursor;
				lab2: do {
					// (, line 35
					if (!(I_p1 > 7)) {
						cursor = limit - v_4;
						break lab2;
					}
					// [, line 36
					ket = cursor;
					// substring, line 36
					among_var = find_among_b(a_1);
					if (among_var == 0) {
						cursor = limit - v_4;
						break lab2;
					}
					// ], line 36
					bra = cursor;
					switch (among_var) {
					case 0:
						cursor = limit - v_4;
						break lab2;
					case 1:
						// (, line 36
						// delete, line 36
						slice_del();
						break;
					}
				} while (false);
				I_p1 = (current.length());
				// try, line 39
				v_5 = limit - cursor;
				lab3: do {
					// (, line 39
					if (!(I_p1 > 6)) {
						cursor = limit - v_5;
						break lab3;
					}
					// [, line 40
					ket = cursor;
					// substring, line 40
					among_var = find_among_b(a_2);
					if (among_var == 0) {
						cursor = limit - v_5;
						break lab3;
					}
					// ], line 40
					bra = cursor;
					switch (among_var) {
					case 0:
						cursor = limit - v_5;
						break lab3;
					case 1:
						// (, line 43
						// delete, line 43
						slice_del();
						break;
					}
				} while (false);
				I_p1 = (current.length());
				// try, line 46
				v_6 = limit - cursor;
				lab4: do {
					// (, line 46
					if (!(I_p1 > 6)) {
						cursor = limit - v_6;
						break lab4;
					}
					// [, line 47
					ket = cursor;
					// substring, line 47
					among_var = find_among_b(a_3);
					if (among_var == 0) {
						cursor = limit - v_6;
						break lab4;
					}
					// ], line 47
					bra = cursor;
					switch (among_var) {
					case 0:
						cursor = limit - v_6;
						break lab4;
					case 1:
						// (, line 50
						// delete, line 50
						slice_del();
						break;
					}
				} while (false);
				I_p1 = (current.length());
				// try, line 53
				v_7 = limit - cursor;
				lab5: do {
					// (, line 53
					if (!(I_p1 > 5)) {
						cursor = limit - v_7;
						break lab5;
					}
					// [, line 54
					ket = cursor;
					// substring, line 54
					among_var = find_among_b(a_4);
					if (among_var == 0) {
						cursor = limit - v_7;
						break lab5;
					}
					// ], line 54
					bra = cursor;
					switch (among_var) {
					case 0:
						cursor = limit - v_7;
						break lab5;
					case 1:
						// (, line 55
						// delete, line 55
						slice_del();
						break;
					}
				} while (false);
				I_p1 = (current.length());
				// try, line 58
				v_8 = limit - cursor;
				lab6: do {
					// (, line 58
					// (, line 58
					if (!(I_p1 > 6)) {
						cursor = limit - v_8;
						break lab6;
					}
					// (, line 58
					// [, line 59
					ket = cursor;
					if (!(in_grouping_b(g_soglasniki, 98, 382))) {
						cursor = limit - v_8;
						break lab6;
					}
					// ], line 59
					bra = cursor;
					// test, line 59
					v_9 = limit - cursor;
					if (!(in_grouping_b(g_soglasniki, 98, 382))) {
						cursor = limit - v_8;
						break lab6;
					}
					cursor = limit - v_9;
					// delete, line 59
					slice_del();
				} while (false);
				I_p1 = (current.length());
				// try, line 63
				v_10 = limit - cursor;
				lab7: do {
					// (, line 63
					if (!(I_p1 > 5)) {
						cursor = limit - v_10;
						break lab7;
					}
					// [, line 64
					ket = cursor;
					// substring, line 64
					among_var = find_among_b(a_5);
					if (among_var == 0) {
						cursor = limit - v_10;
						break lab7;
					}
					// ], line 64
					bra = cursor;
					switch (among_var) {
					case 0:
						cursor = limit - v_10;
						break lab7;
					case 1:
						// (, line 64
						// delete, line 64
						slice_del();
						break;
					}
				} while (false);
			}
		} while (false);
		cursor = limit - v_1;
		cursor = limit_backward;
		return true;
	}
}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class spanishStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("", -1, 6), new Among("\u00E1", 0, 1), new Among("\u00E9", 0, 2),
			new Among("\u00ED", 0, 3), new Among("\u00F3", 0, 4), new Among("\u00FA", 0, 5) };

	private final static Among a_1[] = { new Among("la", -1, -1), new Among("sela", 0, -1), new Among("le", -1, -1),
			new Among("me", -1, -1), new Among("se", -1, -1), new Among("lo", -1, -1), new Among("selo", 5, -1),
			new Among("las", -1, -1), new Among("selas", 7, -1), new Among("les", -1, -1), new Among("los", -1, -1),
			new Among("selos", 10, -1), new Among("nos", -1, -1) };

	private final static Among a_2[] = { new Among("ando", -1, 6), new Among("iendo", -1, 6), new Among("yendo", -1, 7),
			new Among("\u00E1ndo", -1, 2), new Among("i\u00E9ndo", -1, 1), new Among("ar", -1, 6),
			new Among("er", -1, 6), new Among("ir", -1, 6), new Among("\u00E1r", -1, 3), new Among("\u00E9r", -1, 4),
			new Among("\u00EDr", -1, 5) };

	private final static Among a_3[] = { new Among("ic", -1, -1), new Among("ad", -1, -1), new Among("os", -1, -1),
			new Among("iv", -1, 1) };

	private final static Among a_4[] = { new Among("able", -1, 1), new Among("ible", -1, 1), new Among("ante", -1, 1) };

	private final static Among a_5[] = { new Among("ic", -1, 1), new Among("abil", -1, 1), new Among("iv", -1, 1) };

	private final static Among a_6[] = { new Among("ica", -1, 1), new Among("ancia", -1, 2), new Among("encia", -1, 5),
			new Among("adora", -1, 2), new Among("osa", -1, 1), new Among("ista", -1, 1), new Among("iva", -1, 9),
			new Among("anza", -1, 1), new Among("log\u00EDa", -1, 3), new Among("idad", -1, 8),
			new Among("able", -1, 1), new Among("ible", -1, 1), new Among("ante", -1, 2), new Among("mente", -1, 7),
			new Among("amente", 13, 6), new Among("aci\u00F3n", -1, 2), new Among("uci\u00F3n", -1, 4),
			new Among("ico", -1, 1), new Among("ismo", -1, 1), new Among("oso", -1, 1), new Among("amiento", -1, 1),
			new Among("imiento", -1, 1), new Among("ivo", -1, 9), new Among("ador", -1, 2), new Among("icas", -1, 1),
			new Among("ancias", -1, 2), new Among("encias", -1, 5), new Among("adoras", -1, 2),
			new Among("osas", -1, 1), new Among("istas", -1, 1), new Among("ivas", -1, 9), new Among("anzas", -1, 1),
			new Among("log\u00EDas", -1, 3), new Among("idades", -1, 8), new Among("ables", -1, 1),
			new Among("ibles", -1, 1), new Among("aciones", -1, 2), new Among("uciones", -1, 4),
			new Among("adores", -1, 2), new Among("antes", -1, 2), new Among("icos", -1, 1), new Among("ismos", -1, 1),
			new Among("osos", -1, 1), new Among("amientos", -1, 1), new Among("imientos", -1, 1),
			new Among("ivos", -1, 9) };

	private final static Among a_7[] = { new Among("ya", -1, 1), new Among("ye", -1, 1), new Among("yan", -1, 1),
			new Among("yen", -1, 1), new Among("yeron", -1, 1), new Among("yendo", -1, 1), new Among("yo", -1, 1),
			new Among("yas", -1, 1), new Among("yes", -1, 1), new Among("yais", -1, 1), new Among("yamos", -1, 1),
			new Among("y\u00F3", -1, 1) };

	private final static Among a_8[] = { new Among("aba", -1, 2), new Among("ada", -1, 2), new Among("ida", -1, 2),
			new Among("ara", -1, 2), new Among("iera", -1, 2), new Among("\u00EDa", -1, 2),
			new Among("ar\u00EDa", 5, 2), new Among("er\u00EDa", 5, 2), new Among("ir\u00EDa", 5, 2),
			new Among("ad", -1, 2), new Among("ed", -1, 2), new Among("id", -1, 2), new Among("ase", -1, 2),
			new Among("iese", -1, 2), new Among("aste", -1, 2), new Among("iste", -1, 2), new Among("an", -1, 2),
			new Among("aban", 16, 2), new Among("aran", 16, 2), new Among("ieran", 16, 2), new Among("\u00EDan", 16, 2),
			new Among("ar\u00EDan", 20, 2), new Among("er\u00EDan", 20, 2), new Among("ir\u00EDan", 20, 2),
			new Among("en", -1, 1), new Among("asen", 24, 2), new Among("iesen", 24, 2), new Among("aron", -1, 2),
			new Among("ieron", -1, 2), new Among("ar\u00E1n", -1, 2), new Among("er\u00E1n", -1, 2),
			new Among("ir\u00E1n", -1, 2), new Among("ado", -1, 2), new Among("ido", -1, 2), new Among("ando", -1, 2),
			new Among("iendo", -1, 2), new Among("ar", -1, 2), new Among("er", -1, 2), new Among("ir", -1, 2),
			new Among("as", -1, 2), new Among("abas", 39, 2), new Among("adas", 39, 2), new Among("idas", 39, 2),
			new Among("aras", 39, 2), new Among("ieras", 39, 2), new Among("\u00EDas", 39, 2),
			new Among("ar\u00EDas", 45, 2), new Among("er\u00EDas", 45, 2), new Among("ir\u00EDas", 45, 2),
			new Among("es", -1, 1), new Among("ases", 49, 2), new Among("ieses", 49, 2), new Among("abais", -1, 2),
			new Among("arais", -1, 2), new Among("ierais", -1, 2), new Among("\u00EDais", -1, 2),
			new Among("ar\u00EDais", 55, 2), new Among("er\u00EDais", 55, 2), new Among("ir\u00EDais", 55, 2),
			new Among("aseis", -1, 2), new Among("ieseis", -1, 2), new Among("asteis", -1, 2),
			new Among("isteis", -1, 2), new Among("\u00E1is", -1, 2), new Among("\u00E9is", -1, 1),
			new Among("ar\u00E9is", 64, 2), new Among("er\u00E9is", 64, 2), new Among("ir\u00E9is", 64, 2),
			new Among("ados", -1, 2), new Among("idos", -1, 2), new Among("amos", -1, 2),
			new Among("\u00E1bamos", 70, 2), new Among("\u00E1ramos", 70, 2), new Among("i\u00E9ramos", 70, 2),
			new Among("\u00EDamos", 70, 2), new Among("ar\u00EDamos", 74, 2), new Among("er\u00EDamos", 74, 2),
			new Among("ir\u00EDamos", 74, 2), new Among("emos", -1, 1), new Among("aremos", 78, 2),
			new Among("eremos", 78, 2), new Among("iremos", 78, 2), new Among("\u00E1semos", 78, 2),
			new Among("i\u00E9semos", 78, 2), new Among("imos", -1, 2), new Among("ar\u00E1s", -1, 2),
			new Among("er\u00E1s", -1, 2), new Among("ir\u00E1s", -1, 2), new Among("\u00EDs", -1, 2),
			new Among("ar\u00E1", -1, 2), new Among("er\u00E1", -1, 2), new Among("ir\u00E1", -1, 2),
			new Among("ar\u00E9", -1, 2), new Among("er\u00E9", -1, 2), new Among("ir\u00E9", -1, 2),
			new Among("i\u00F3", -1, 2) };

	private final static Among a_9[] = { new Among("a", -1, 1), new Among("e", -1, 2), new Among("o", -1, 1),
			new Among("os", -1, 1), new Among("\u00E1", -1, 1), new Among("\u00E9", -1, 2), new Among("\u00ED", -1, 1),
			new Among("\u00F3", -1, 1) };

	private static final char g_v[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 4, 10 };

	private int I_p2;
	private int I_p1;
	private int I_pV;

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		int v_3;
		int v_6;
		int v_8;
		// (, line 31
		I_pV = limit;
		I_p1 = limit;
		I_p2 = limit;
		// do, line 37
		v_1 = cursor;
		lab0: do {
			// (, line 37
			// or, line 39
			lab1: do {
				v_2 = cursor;
				lab2: do {
					// (, line 38
					if (!(in_grouping(g_v, 97, 252))) {
						break lab2;
					}
					// or, line 38
					lab3: do {
						v_3 = cursor;
						lab4: do {
							// (, line 38
							if (!(out_grouping(g_v, 97, 252))) {
								break lab4;
							}
							// gopast, line 38
							golab5: while (true) {
								lab6: do {
									if (!(in_grouping(g_v, 97, 252))) {
										break lab6;
									}
									break golab5;
								} while (false);
								if (cursor >= limit) {
									break lab4;
								}
								cursor++;
							}
							break lab3;
						} while (false);
						cursor = v_3;
						// (, line 38
						if (!(in_grouping(g_v, 97, 252))) {
							break lab2;
						}
						// gopast, line 38
						golab7: while (true) {
							lab8: do {
								if (!(out_grouping(g_v, 97, 252))) {
									break lab8;
								}
								break golab7;
							} while (false);
							if (cursor >= limit) {
								break lab2;
							}
							cursor++;
						}
					} while (false);
					break lab1;
				} while (false);
				cursor = v_2;
				// (, line 40
				if (!(out_grouping(g_v, 97, 252))) {
					break lab0;
				}
				// or, line 40
				lab9: do {
					v_6 = cursor;
					lab10: do {
						// (, line 40
						if (!(out_grouping(g_v, 97, 252))) {
							break lab10;
						}
						// gopast, line 40
						golab11: while (true) {
							lab12: do {
								if (!(in_grouping(g_v, 97, 252))) {
									break lab12;
								}
								break golab11;
							} while (false);
							if (cursor >= limit) {
								break lab10;
							}
							cursor++;
						}
						break lab9;
					} while (false);
					cursor = v_6;
					// (, line 40
					if (!(in_grouping(g_v, 97, 252))) {
						break lab0;
					}
					// next, line 40
					if (cursor >= limit) {
						break lab0;
					}
					cursor++;
				} while (false);
			} while (false);
			// setmark pV, line 41
			I_pV = cursor;
		} while (false);
		cursor = v_1;
		// do, line 43
		v_8 = cursor;
		lab13: do {
			// (, line 43
			// gopast, line 44
			golab14: while (true) {
				lab15: do {
					if (!(in_grouping(g_v, 97, 252))) {
						break lab15;
					}
					break golab14;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 44
			golab16: while (true) {
				lab17: do {
					if (!(out_grouping(g_v, 97, 252))) {
						break lab17;
					}
					break golab16;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p1, line 44
			I_p1 = cursor;
			// gopast, line 45
			golab18: while (true) {
				lab19: do {
					if (!(in_grouping(g_v, 97, 252))) {
						break lab19;
					}
					break golab18;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// gopast, line 45
			golab20: while (true) {
				lab21: do {
					if (!(out_grouping(g_v, 97, 252))) {
						break lab21;
					}
					break golab20;
				} while (false);
				if (cursor >= limit) {
					break lab13;
				}
				cursor++;
			}
			// setmark p2, line 45
			I_p2 = cursor;
		} while (false);
		cursor = v_8;
		return true;
	}

	private boolean r_postlude() {
		int among_var;
		int v_1;
		// repeat, line 49
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 49
				// [, line 50
				bra = cursor;
				// substring, line 50
				among_var = find_among(a_0);
				if (among_var == 0) {
					break lab1;
				}
				// ], line 50
				ket = cursor;
				switch (among_var) {
				case 0:
					break lab1;
				case 1:
					// (, line 51
					// <-, line 51
					slice_from("a");
					break;
				case 2:
					// (, line 52
					// <-, line 52
					slice_from("e");
					break;
				case 3:
					// (, line 53
					// <-, line 53
					slice_from("i");
					break;
				case 4:
					// (, line 54
					// <-, line 54
					slice_from("o");
					break;
				case 5:
					// (, line 55
					// <-, line 55
					slice_from("u");
					break;
				case 6:
					// (, line 57
					// next, line 57
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
					break;
				}
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_RV() {
		if (!(I_pV <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R1() {
		if (!(I_p1 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_R2() {
		if (!(I_p2 <= cursor)) {
			return false;
		}
		return true;
	}

	private boolean r_attached_pronoun() {
		int among_var;
		// (, line 67
		// [, line 68
		ket = cursor;
		// substring, line 68
		if (find_among_b(a_1) == 0) {
			return false;
		}
		// ], line 68
		bra = cursor;
		// substring, line 72
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			return false;
		}
		// call RV, line 72
		if (!r_RV()) {
			return false;
		}
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 73
			// ], line 73
			bra = cursor;
			// <-, line 73
			slice_from("iendo");
			break;
		case 2:
			// (, line 74
			// ], line 74
			bra = cursor;
			// <-, line 74
			slice_from("ando");
			break;
		case 3:
			// (, line 75
			// ], line 75
			bra = cursor;
			// <-, line 75
			slice_from("ar");
			break;
		case 4:
			// (, line 76
			// ], line 76
			bra = cursor;
			// <-, line 76
			slice_from("er");
			break;
		case 5:
			// (, line 77
			// ], line 77
			bra = cursor;
			// <-, line 77
			slice_from("ir");
			break;
		case 6:
			// (, line 81
			// delete, line 81
			slice_del();
			break;
		case 7:
			// (, line 82
			// literal, line 82
			if (!(eq_s_b("u"))) {
				return false;
			}
			// delete, line 82
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_standard_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 86
		// [, line 87
		ket = cursor;
		// substring, line 87
		among_var = find_among_b(a_6);
		if (among_var == 0) {
			return false;
		}
		// ], line 87
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 98
			// call R2, line 99
			if (!r_R2()) {
				return false;
			}
			// delete, line 99
			slice_del();
			break;
		case 2:
			// (, line 104
			// call R2, line 105
			if (!r_R2()) {
				return false;
			}
			// delete, line 105
			slice_del();
			// try, line 106
			v_1 = limit - cursor;
			lab0: do {
				// (, line 106
				// [, line 106
				ket = cursor;
				// literal, line 106
				if (!(eq_s_b("ic"))) {
					cursor = limit - v_1;
					break lab0;
				}
				// ], line 106
				bra = cursor;
				// call R2, line 106
				if (!r_R2()) {
					cursor = limit - v_1;
					break lab0;
				}
				// delete, line 106
				slice_del();
			} while (false);
			break;
		case 3:
			// (, line 110
			// call R2, line 111
			if (!r_R2()) {
				return false;
			}
			// <-, line 111
			slice_from("log");
			break;
		case 4:
			// (, line 114
			// call R2, line 115
			if (!r_R2()) {
				return false;
			}
			// <-, line 115
			slice_from("u");
			break;
		case 5:
			// (, line 118
			// call R2, line 119
			if (!r_R2()) {
				return false;
			}
			// <-, line 119
			slice_from("ente");
			break;
		case 6:
			// (, line 122
			// call R1, line 123
			if (!r_R1()) {
				return false;
			}
			// delete, line 123
			slice_del();
			// try, line 124
			v_2 = limit - cursor;
			lab1: do {
				// (, line 124
				// [, line 125
				ket = cursor;
				// substring, line 125
				among_var = find_among_b(a_3);
				if (among_var == 0) {
					cursor = limit - v_2;
					break lab1;
				}
				// ], line 125
				bra = cursor;
				// call R2, line 125
				if (!r_R2()) {
					cursor = limit - v_2;
					break lab1;
				}
				// delete, line 125
				slice_del();
				switch (among_var) {
				case 0:
					cursor = limit - v_2;
					break lab1;
				case 1:
					// (, line 126
					// [, line 126
					ket = cursor;
					// literal, line 126
					if (!(eq_s_b("at"))) {
						cursor = limit - v_2;
						break lab1;
					}
					// ], line 126
					bra = cursor;
					// call R2, line 126
					if (!r_R2()) {
						cursor = limit - v_2;
						break lab1;
					}
					// delete, line 126
					slice_del();
					break;
				}
			} while (false);
			break;
		case 7:
			// (, line 134
			// call R2, line 135
			if (!r_R2()) {
				return false;
			}
			// delete, line 135
			slice_del();
			// try, line 136
			v_3 = limit - cursor;
			lab2: do {
				// (, line 136
				// [, line 137
				ket = cursor;
				// substring, line 137
				among_var = find_among_b(a_4);
				if (among_var == 0) {
					cursor = limit - v_3;
					break lab2;
				}
				// ], line 137
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_3;
					break lab2;
				case 1:
					// (, line 140
					// call R2, line 140
					if (!r_R2()) {
						cursor = limit - v_3;
						break lab2;
					}
					// delete, line 140
					slice_del();
					break;
				}
			} while (false);
			break;
		case 8:
			// (, line 146
			// call R2, line 147
			if (!r_R2()) {
				return false;
			}
			// delete, line 147
			slice_del();
			// try, line 148
			v_4 = limit - cursor;
			lab3: do {
				// (, line 148
				// [, line 149
				ket = cursor;
				// substring, line 149
				among_var = find_among_b(a_5);
				if (among_var == 0) {
					cursor = limit - v_4;
					break lab3;
				}
				// ], line 149
				bra = cursor;
				switch (among_var) {
				case 0:
					cursor = limit - v_4;
					break lab3;
				case 1:
					// (, line 152
					// call R2, line 152
					if (!r_R2()) {
						cursor = limit - v_4;
						break lab3;
					}
					// delete, line 152
					slice_del();
					break;
				}
			} while (false);
			break;
		case 9:
			// (, line 158
			// call R2, line 159
			if (!r_R2()) {
				return false;
			}
			// delete, line 159
			slice_del();
			// try, line 160
			v_5 = limit - cursor;
			lab4: do {
				// (, line 160
				// [, line 161
				ket = cursor;
				// literal, line 161
				if (!(eq_s_b("at"))) {
					cursor = limit - v_5;
					break lab4;
				}
				// ], line 161
				bra = cursor;
				// call R2, line 161
				if (!r_R2()) {
					cursor = limit - v_5;
					break lab4;
				}
				// delete, line 161
				slice_del();
			} while (false);
			break;
		}
		return true;
	}

	private boolean r_y_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 167
		// setlimit, line 168
		v_1 = limit - cursor;
		// tomark, line 168
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 168
		// [, line 168
		ket = cursor;
		// substring, line 168
		among_var = find_among_b(a_7);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 168
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 171
			// literal, line 171
			if (!(eq_s_b("u"))) {
				return false;
			}
			// delete, line 171
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_verb_suffix() {
		int among_var;
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 175
		// setlimit, line 176
		v_1 = limit - cursor;
		// tomark, line 176
		if (cursor < I_pV) {
			return false;
		}
		cursor = I_pV;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 176
		// [, line 176
		ket = cursor;
		// substring, line 176
		among_var = find_among_b(a_8);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 176
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 179
			// try, line 179
			v_3 = limit - cursor;
			lab0: do {
				// (, line 179
				// literal, line 179
				if (!(eq_s_b("u"))) {
					cursor = limit - v_3;
					break lab0;
				}
				// test, line 179
				v_4 = limit - cursor;
				// literal, line 179
				if (!(eq_s_b("g"))) {
					cursor = limit - v_3;
					break lab0;
				}
				cursor = limit - v_4;
			} while (false);
			// ], line 179
			bra = cursor;
			// delete, line 179
			slice_del();
			break;
		case 2:
			// (, line 200
			// delete, line 200
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_residual_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 204
		// [, line 205
		ket = cursor;
		// substring, line 205
		among_var = find_among_b(a_9);
		if (among_var == 0) {
			return false;
		}
		// ], line 205
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 208
			// call RV, line 208
			if (!r_RV()) {
				return false;
			}
			// delete, line 208
			slice_del();
			break;
		case 2:
			// (, line 210
			// call RV, line 210
			if (!r_RV()) {
				return false;
			}
			// delete, line 210
			slice_del();
			// try, line 210
			v_1 = limit - cursor;
			lab0: do {
				// (, line 210
				// [, line 210
				ket = cursor;
				// literal, line 210
				if (!(eq_s_b("u"))) {
					cursor = limit - v_1;
					break lab0;
				}
				// ], line 210
				bra = cursor;
				// test, line 210
				v_2 = limit - cursor;
				// literal, line 210
				if (!(eq_s_b("g"))) {
					cursor = limit - v_1;
					break lab0;
				}
				cursor = limit - v_2;
				// call RV, line 210
				if (!r_RV()) {
					cursor = limit - v_1;
					break lab0;
				}
				// delete, line 210
				slice_del();
			} while (false);
			break;
		}
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		// (, line 215
		// do, line 216
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 216
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 217
		limit_backward = cursor;
		cursor = limit;
		// (, line 217
		// do, line 218
		v_2 = limit - cursor;
		lab1: do {
			// call attached_pronoun, line 218
			if (!r_attached_pronoun()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 219
		v_3 = limit - cursor;
		lab2: do {
			// (, line 219
			// or, line 219
			lab3: do {
				v_4 = limit - cursor;
				lab4: do {
					// call standard_suffix, line 219
					if (!r_standard_suffix()) {
						break lab4;
					}
					break lab3;
				} while (false);
				cursor = limit - v_4;
				lab5: do {
					// call y_verb_suffix, line 220
					if (!r_y_verb_suffix()) {
						break lab5;
					}
					break lab3;
				} while (false);
				cursor = limit - v_4;
				// call verb_suffix, line 221
				if (!r_verb_suffix()) {
					break lab2;
				}
			} while (false);
		} while (false);
		cursor = limit - v_3;
		// do, line 223
		v_5 = limit - cursor;
		lab6: do {
			// call residual_suffix, line 223
			if (!r_residual_suffix()) {
				break lab6;
			}
		} while (false);
		cursor = limit - v_5;
		cursor = limit_backward; // do, line 225
		v_6 = cursor;
		lab7: do {
			// call postlude, line 225
			if (!r_postlude()) {
				break lab7;
			}
		} while (false);
		cursor = v_6;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class swedishStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("a", -1, 1), new Among("arna", 0, 1), new Among("erna", 0, 1),
			new Among("heterna", 2, 1), new Among("orna", 0, 1), new Among("ad", -1, 1), new Among("e", -1, 1),
			new Among("ade", 6, 1), new Among("ande", 6, 1), new Among("arne", 6, 1), new Among("are", 6, 1),
			new Among("aste", 6, 1), new Among("en", -1, 1), new Among("anden", 12, 1), new Among("aren", 12, 1),
			new Among("heten", 12, 1), new Among("ern", -1, 1), new Among("ar", -1, 1), new Among("er", -1, 1),
			new Among("heter", 18, 1), new Among("or", -1, 1), new Among("s", -1, 2), new Among("as", 21, 1),
			new Among("arnas", 22, 1), new Among("ernas", 22, 1), new Among("ornas", 22, 1), new Among("es", 21, 1),
			new Among("ades", 26, 1), new Among("andes", 26, 1), new Among("ens", 21, 1), new Among("arens", 29, 1),
			new Among("hetens", 29, 1), new Among("erns", 21, 1), new Among("at", -1, 1), new Among("andet", -1, 1),
			new Among("het", -1, 1), new Among("ast", -1, 1) };

	private final static Among a_1[] = { new Among("dd", -1, -1), new Among("gd", -1, -1), new Among("nn", -1, -1),
			new Among("dt", -1, -1), new Among("gt", -1, -1), new Among("kt", -1, -1), new Among("tt", -1, -1) };

	private final static Among a_2[] = { new Among("ig", -1, 1), new Among("lig", 0, 1), new Among("els", -1, 1),
			new Among("fullt", -1, 3), new Among("l\u00F6st", -1, 2) };

	private static final char g_v[] = { 17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 32 };

	private static final char g_s_ending[] = { 119, 127, 149 };

	private int I_x;
	private int I_p1;

	private boolean r_mark_regions() {
		int v_1;
		int v_2;
		// (, line 26
		I_p1 = limit;
		// test, line 29
		v_1 = cursor;
		// (, line 29
		// hop, line 29
		{
			int c = cursor + 3;
			if (0 > c || c > limit) {
				return false;
			}
			cursor = c;
		}
		// setmark x, line 29
		I_x = cursor;
		cursor = v_1;
		// goto, line 30
		golab0: while (true) {
			v_2 = cursor;
			lab1: do {
				if (!(in_grouping(g_v, 97, 246))) {
					break lab1;
				}
				cursor = v_2;
				break golab0;
			} while (false);
			cursor = v_2;
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// gopast, line 30
		golab2: while (true) {
			lab3: do {
				if (!(out_grouping(g_v, 97, 246))) {
					break lab3;
				}
				break golab2;
			} while (false);
			if (cursor >= limit) {
				return false;
			}
			cursor++;
		}
		// setmark p1, line 30
		I_p1 = cursor;
		// try, line 31
		lab4: do {
			// (, line 31
			if (!(I_p1 < I_x)) {
				break lab4;
			}
			I_p1 = I_x;
		} while (false);
		return true;
	}

	private boolean r_main_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// (, line 36
		// setlimit, line 37
		v_1 = limit - cursor;
		// tomark, line 37
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 37
		// [, line 37
		ket = cursor;
		// substring, line 37
		among_var = find_among_b(a_0);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 37
		bra = cursor;
		limit_backward = v_2;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 44
			// delete, line 44
			slice_del();
			break;
		case 2:
			// (, line 46
			if (!(in_grouping_b(g_s_ending, 98, 121))) {
				return false;
			}
			// delete, line 46
			slice_del();
			break;
		}
		return true;
	}

	private boolean r_consonant_pair() {
		int v_1;
		int v_2;
		int v_3;
		// setlimit, line 50
		v_1 = limit - cursor;
		// tomark, line 50
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 50
		// and, line 52
		v_3 = limit - cursor;
		// among, line 51
		if (find_among_b(a_1) == 0) {
			limit_backward = v_2;
			return false;
		}
		cursor = limit - v_3;
		// (, line 52
		// [, line 52
		ket = cursor;
		// next, line 52
		if (cursor <= limit_backward) {
			limit_backward = v_2;
			return false;
		}
		cursor--;
		// ], line 52
		bra = cursor;
		// delete, line 52
		slice_del();
		limit_backward = v_2;
		return true;
	}

	private boolean r_other_suffix() {
		int among_var;
		int v_1;
		int v_2;
		// setlimit, line 55
		v_1 = limit - cursor;
		// tomark, line 55
		if (cursor < I_p1) {
			return false;
		}
		cursor = I_p1;
		v_2 = limit_backward;
		limit_backward = cursor;
		cursor = limit - v_1;
		// (, line 55
		// [, line 56
		ket = cursor;
		// substring, line 56
		among_var = find_among_b(a_2);
		if (among_var == 0) {
			limit_backward = v_2;
			return false;
		}
		// ], line 56
		bra = cursor;
		switch (among_var) {
		case 0:
			limit_backward = v_2;
			return false;
		case 1:
			// (, line 57
			// delete, line 57
			slice_del();
			break;
		case 2:
			// (, line 58
			// <-, line 58
			slice_from("l\u00F6s");
			break;
		case 3:
			// (, line 59
			// <-, line 59
			slice_from("full");
			break;
		}
		limit_backward = v_2;
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		// (, line 64
		// do, line 66
		v_1 = cursor;
		lab0: do {
			// call mark_regions, line 66
			if (!r_mark_regions()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// backwards, line 67
		limit_backward = cursor;
		cursor = limit;
		// (, line 67
		// do, line 68
		v_2 = limit - cursor;
		lab1: do {
			// call main_suffix, line 68
			if (!r_main_suffix()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 69
		v_3 = limit - cursor;
		lab2: do {
			// call consonant_pair, line 69
			if (!r_consonant_pair()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		// do, line 70
		v_4 = limit - cursor;
		lab3: do {
			// call other_suffix, line 70
			if (!r_other_suffix()) {
				break lab3;
			}
		} while (false);
		cursor = limit - v_4;
		cursor = limit_backward;
		return true;
	}

}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class tamilStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("\u0B95", -1, -1), new Among("\u0B99", -1, -1),
			new Among("\u0B9A", -1, -1), new Among("\u0B9E", -1, -1), new Among("\u0BA4", -1, -1),
			new Among("\u0BA8", -1, -1), new Among("\u0BAA", -1, -1), new Among("\u0BAE", -1, -1),
			new Among("\u0BAF", -1, -1), new Among("\u0BB5", -1, -1) };

	private final static Among a_1[] = { new Among("\u0BA8\u0BCD\u0BA4", -1, -1),
			new Among("\u0BA8\u0BCD\u0BA4\u0BCD", -1, -1), new Among("\u0BA8\u0BCD", -1, -1) };

	private final static Among a_2[] = { new Among("\u0BBF", -1, -1), new Among("\u0BC0", -1, -1),
			new Among("\u0BC8", -1, -1) };

	private final static Among a_3[] = { new Among("\u0B95", -1, -1), new Among("\u0B9A", -1, -1),
			new Among("\u0B9F", -1, -1), new Among("\u0BA4", -1, -1), new Among("\u0BAA", -1, -1),
			new Among("\u0BB1", -1, -1) };

	private final static Among a_4[] = { new Among("\u0B95", -1, -1), new Among("\u0B9A", -1, -1),
			new Among("\u0B9F", -1, -1), new Among("\u0BA4", -1, -1), new Among("\u0BAA", -1, -1),
			new Among("\u0BB1", -1, -1) };

	private final static Among a_5[] = { new Among("\u0B95", -1, -1), new Among("\u0B9A", -1, -1),
			new Among("\u0B9F", -1, -1), new Among("\u0BA4", -1, -1), new Among("\u0BAA", -1, -1),
			new Among("\u0BB1", -1, -1) };

	private final static Among a_6[] = { new Among("\u0BAF", -1, -1), new Among("\u0BB0", -1, -1),
			new Among("\u0BB2", -1, -1), new Among("\u0BB3", -1, -1), new Among("\u0BB4", -1, -1),
			new Among("\u0BB5", -1, -1) };

	private final static Among a_7[] = { new Among("\u0B99", -1, -1), new Among("\u0B9E", -1, -1),
			new Among("\u0BA3", -1, -1), new Among("\u0BA8", -1, -1), new Among("\u0BA9", -1, -1),
			new Among("\u0BAE", -1, -1) };

	private final static Among a_8[] = { new Among("\u0BAF", -1, -1), new Among("\u0BB5", -1, -1),
			new Among("\u0BB5\u0BCD", -1, -1) };

	private final static Among a_9[] = { new Among("\u0BBE", -1, -1), new Among("\u0BBF", -1, -1),
			new Among("\u0BC0", -1, -1), new Among("\u0BC1", -1, -1), new Among("\u0BC2", -1, -1),
			new Among("\u0BC6", -1, -1), new Among("\u0BC7", -1, -1), new Among("\u0BC8", -1, -1) };

	private final static Among a_10[] = { new Among("\u0BBE", -1, -1), new Among("\u0BBF", -1, -1),
			new Among("\u0BC0", -1, -1), new Among("\u0BC1", -1, -1), new Among("\u0BC2", -1, -1),
			new Among("\u0BC6", -1, -1), new Among("\u0BC7", -1, -1), new Among("\u0BC8", -1, -1) };

	private final static Among a_11[] = { new Among("\u0B85", -1, -1), new Among("\u0B87", -1, -1),
			new Among("\u0B89", -1, -1) };

	private final static Among a_12[] = { new Among("\u0B95", -1, -1), new Among("\u0B99", -1, -1),
			new Among("\u0B9A", -1, -1), new Among("\u0B9E", -1, -1), new Among("\u0BA4", -1, -1),
			new Among("\u0BA8", -1, -1), new Among("\u0BAA", -1, -1), new Among("\u0BAE", -1, -1),
			new Among("\u0BAF", -1, -1), new Among("\u0BB5", -1, -1) };

	private final static Among a_13[] = { new Among("\u0B95", -1, -1), new Among("\u0B9A", -1, -1),
			new Among("\u0B9F", -1, -1), new Among("\u0BA4", -1, -1), new Among("\u0BAA", -1, -1),
			new Among("\u0BB1", -1, -1) };

	private final static Among a_14[] = { new Among("\u0BBE", -1, -1), new Among("\u0BC7", -1, -1),
			new Among("\u0BCB", -1, -1) };

	private final static Among a_15[] = { new Among("\u0BAA\u0BBF", -1, -1), new Among("\u0BB5\u0BBF", -1, -1) };

	private final static Among a_16[] = { new Among("\u0BBE", -1, -1), new Among("\u0BBF", -1, -1),
			new Among("\u0BC0", -1, -1), new Among("\u0BC1", -1, -1), new Among("\u0BC2", -1, -1),
			new Among("\u0BC6", -1, -1), new Among("\u0BC7", -1, -1), new Among("\u0BC8", -1, -1) };

	private final static Among a_17[] = { new Among("\u0BAA\u0B9F\u0BCD\u0B9F", -1, -1),
			new Among("\u0BAA\u0B9F\u0BCD\u0B9F\u0BA3", -1, -1), new Among("\u0BA4\u0BBE\u0BA9", -1, -1),
			new Among("\u0BAA\u0B9F\u0BBF\u0BA4\u0BBE\u0BA9", 2, -1),
			new Among("\u0B95\u0BC1\u0BB0\u0BBF\u0BAF", -1, -1), new Among("\u0BAA\u0B9F\u0BBF", -1, -1),
			new Among("\u0BAA\u0BB1\u0BCD\u0BB1\u0BBF", -1, -1), new Among("\u0BAA\u0B9F\u0BC1", -1, -1),
			new Among("\u0BB5\u0BBF\u0B9F\u0BC1", -1, -1), new Among("\u0BAA\u0B9F\u0BCD\u0B9F\u0BC1", -1, -1),
			new Among("\u0BB5\u0BBF\u0B9F\u0BCD\u0B9F\u0BC1", -1, -1),
			new Among("\u0BAA\u0B9F\u0BCD\u0B9F\u0BA4\u0BC1", -1, -1),
			new Among("\u0BC6\u0BB2\u0BCD\u0BB2\u0BBE\u0BAE\u0BCD", -1, -1) };

	private final static Among a_18[] = { new Among("\u0B95", -1, -1), new Among("\u0B9A", -1, -1),
			new Among("\u0B9F", -1, -1), new Among("\u0BA4", -1, -1), new Among("\u0BAA", -1, -1),
			new Among("\u0BB1", -1, -1) };

	private final static Among a_19[] = { new Among("\u0B95", -1, -1), new Among("\u0B9A", -1, -1),
			new Among("\u0B9F", -1, -1), new Among("\u0BA4", -1, -1), new Among("\u0BAA", -1, -1),
			new Among("\u0BB1", -1, -1) };

	private final static Among a_20[] = { new Among("\u0BBE", -1, -1), new Among("\u0BBF", -1, -1),
			new Among("\u0BC0", -1, -1), new Among("\u0BC1", -1, -1), new Among("\u0BC2", -1, -1),
			new Among("\u0BC6", -1, -1), new Among("\u0BC7", -1, -1), new Among("\u0BC8", -1, -1) };

	private final static Among a_21[] = { new Among("\u0BBE", -1, -1), new Among("\u0BBF", -1, -1),
			new Among("\u0BC0", -1, -1), new Among("\u0BC1", -1, -1), new Among("\u0BC2", -1, -1),
			new Among("\u0BC6", -1, -1), new Among("\u0BC7", -1, -1), new Among("\u0BC8", -1, -1) };

	private final static Among a_22[] = { new Among("\u0BAA\u0B9F\u0BC1", -1, -1),
			new Among("\u0B95\u0BCA\u0BA3\u0BCD\u0B9F\u0BBF\u0BB0\u0BCD", -1, -1) };

	private final static Among a_23[] = { new Among("\u0B85", -1, -1), new Among("\u0B86", -1, -1),
			new Among("\u0B87", -1, -1), new Among("\u0B88", -1, -1), new Among("\u0B89", -1, -1),
			new Among("\u0B8A", -1, -1), new Among("\u0B8E", -1, -1), new Among("\u0B8F", -1, -1),
			new Among("\u0B90", -1, -1), new Among("\u0B92", -1, -1), new Among("\u0B93", -1, -1),
			new Among("\u0B94", -1, -1) };

	private final static Among a_24[] = { new Among("\u0BBE", -1, -1), new Among("\u0BBF", -1, -1),
			new Among("\u0BC0", -1, -1), new Among("\u0BC1", -1, -1), new Among("\u0BC2", -1, -1),
			new Among("\u0BC6", -1, -1), new Among("\u0BC7", -1, -1), new Among("\u0BC8", -1, -1) };

	private final static Among a_25[] = { new Among("\u0B95\u0BBF\u0BB1", -1, -1),
			new Among("\u0B95\u0BBF\u0BA9\u0BCD\u0BB1", -1, -1),
			new Among("\u0BBE\u0BA8\u0BBF\u0BA9\u0BCD\u0BB1", -1, -1), new Among("\u0B95\u0BBF\u0BB1\u0BCD", -1, -1),
			new Among("\u0B95\u0BBF\u0BA9\u0BCD\u0BB1\u0BCD", -1, -1),
			new Among("\u0BBE\u0BA8\u0BBF\u0BA9\u0BCD\u0BB1\u0BCD", -1, -1) };

	private int I_length;
	private boolean B_found_wrong_ending;
	private boolean B_found_vetrumai_urupu;
	private boolean B_found_a_match;

	private boolean r_has_min_length() {
		// (, line 104
		I_length = (current.length());
		if (!(I_length > 4)) {
			return false;
		}
		return true;
	}

	private boolean r_fix_va_start() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		// (, line 109
		// or, line 110
		lab0: do {
			v_1 = cursor;
			lab1: do {
				// (, line 110
				// and, line 110
				v_2 = cursor;
				// try, line 110
				v_3 = cursor;
				lab2: do {
					// literal, line 110
					if (!(eq_s("\u0BB5\u0BCB"))) {
						cursor = v_3;
						break lab2;
					}
				} while (false);
				cursor = v_2;
				// [, line 110
				bra = cursor;
				// literal, line 110
				if (!(eq_s("\u0BB5\u0BCB"))) {
					break lab1;
				}
				// ], line 110
				ket = cursor;
				// <-, line 110
				slice_from("\u0B93");
				break lab0;
			} while (false);
			cursor = v_1;
			lab3: do {
				// (, line 111
				// and, line 111
				v_4 = cursor;
				// try, line 111
				v_5 = cursor;
				lab4: do {
					// literal, line 111
					if (!(eq_s("\u0BB5\u0BCA"))) {
						cursor = v_5;
						break lab4;
					}
				} while (false);
				cursor = v_4;
				// [, line 111
				bra = cursor;
				// literal, line 111
				if (!(eq_s("\u0BB5\u0BCA"))) {
					break lab3;
				}
				// ], line 111
				ket = cursor;
				// <-, line 111
				slice_from("\u0B92");
				break lab0;
			} while (false);
			cursor = v_1;
			lab5: do {
				// (, line 112
				// and, line 112
				v_6 = cursor;
				// try, line 112
				v_7 = cursor;
				lab6: do {
					// literal, line 112
					if (!(eq_s("\u0BB5\u0BC1"))) {
						cursor = v_7;
						break lab6;
					}
				} while (false);
				cursor = v_6;
				// [, line 112
				bra = cursor;
				// literal, line 112
				if (!(eq_s("\u0BB5\u0BC1"))) {
					break lab5;
				}
				// ], line 112
				ket = cursor;
				// <-, line 112
				slice_from("\u0B89");
				break lab0;
			} while (false);
			cursor = v_1;
			// (, line 113
			// and, line 113
			v_8 = cursor;
			// try, line 113
			v_9 = cursor;
			lab7: do {
				// literal, line 113
				if (!(eq_s("\u0BB5\u0BC2"))) {
					cursor = v_9;
					break lab7;
				}
			} while (false);
			cursor = v_8;
			// [, line 113
			bra = cursor;
			// literal, line 113
			if (!(eq_s("\u0BB5\u0BC2"))) {
				return false;
			}
			// ], line 113
			ket = cursor;
			// <-, line 113
			slice_from("\u0B8A");
		} while (false);
		return true;
	}

	private boolean r_fix_endings() {
		int v_1;
		int v_2;
		// (, line 116
		// set found_wrong_ending, line 117
		B_found_wrong_ending = true;
		// repeat, line 118
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 118
				// Boolean test found_wrong_ending, line 118
				if (!(B_found_wrong_ending)) {
					break lab1;
				}
				// (, line 118
				// do, line 118
				v_2 = cursor;
				lab2: do {
					// call fix_ending, line 118
					if (!r_fix_ending()) {
						break lab2;
					}
				} while (false);
				cursor = v_2;
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_remove_question_prefixes() {
		int v_1;
		// (, line 121
		// [, line 122
		bra = cursor;
		// (, line 122
		// literal, line 122
		if (!(eq_s("\u0B8E"))) {
			return false;
		}
		// among, line 122
		if (find_among(a_0) == 0) {
			return false;
		}
		// literal, line 122
		if (!(eq_s("\u0BCD"))) {
			return false;
		}
		// ], line 122
		ket = cursor;
		// delete, line 122
		slice_del();
		// do, line 123
		v_1 = cursor;
		lab0: do {
			// call fix_va_start, line 123
			if (!r_fix_va_start()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_fix_ending() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		// (, line 126
		// unset found_wrong_ending, line 127
		B_found_wrong_ending = false;
		I_length = (current.length());
		if (!(I_length > 3)) {
			return false;
		}
		// backwards, line 130
		limit_backward = cursor;
		cursor = limit;
		// (, line 130
		// or, line 132
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 131
				// [, line 131
				ket = cursor;
				// among, line 131
				if (find_among_b(a_1) == 0) {
					break lab1;
				}
				// ], line 131
				bra = cursor;
				// delete, line 131
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab2: do {
				// (, line 133
				// [, line 133
				ket = cursor;
				// literal, line 133
				if (!(eq_s_b("\u0BAF\u0BCD"))) {
					break lab2;
				}
				// test, line 133
				v_2 = limit - cursor;
				// among, line 133
				if (find_among_b(a_2) == 0) {
					break lab2;
				}
				cursor = limit - v_2;
				// ], line 133
				bra = cursor;
				// delete, line 133
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab3: do {
				// (, line 135
				// [, line 135
				ket = cursor;
				// or, line 135
				lab4: do {
					v_3 = limit - cursor;
					lab5: do {
						// literal, line 135
						if (!(eq_s_b("\u0B9F\u0BCD\u0BAA\u0BCD"))) {
							break lab5;
						}
						break lab4;
					} while (false);
					cursor = limit - v_3;
					// literal, line 135
					if (!(eq_s_b("\u0B9F\u0BCD\u0B95\u0BCD"))) {
						break lab3;
					}
				} while (false);
				// ], line 135
				bra = cursor;
				// <-, line 135
				slice_from("\u0BB3\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab6: do {
				// (, line 137
				// [, line 137
				ket = cursor;
				// literal, line 137
				if (!(eq_s_b("\u0BA9\u0BCD\u0BB1\u0BCD"))) {
					break lab6;
				}
				// ], line 137
				bra = cursor;
				// <-, line 137
				slice_from("\u0BB2\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab7: do {
				// (, line 140
				// [, line 140
				ket = cursor;
				// literal, line 140
				if (!(eq_s_b("\u0BB1\u0BCD\u0B95\u0BCD"))) {
					break lab7;
				}
				// ], line 140
				bra = cursor;
				// <-, line 140
				slice_from("\u0BB2\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab8: do {
				// (, line 142
				// [, line 142
				ket = cursor;
				// literal, line 142
				if (!(eq_s_b("\u0B9F\u0BCD\u0B9F\u0BCD"))) {
					break lab8;
				}
				// ], line 142
				bra = cursor;
				// <-, line 142
				slice_from("\u0B9F\u0BC1");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab9: do {
				// (, line 144
				// Boolean test found_vetrumai_urupu, line 144
				if (!(B_found_vetrumai_urupu)) {
					break lab9;
				}
				// [, line 144
				ket = cursor;
				// literal, line 144
				if (!(eq_s_b("\u0BA4\u0BCD\u0BA4\u0BCD"))) {
					break lab9;
				}
				// (, line 144
				// test, line 144
				v_4 = limit - cursor;
				// not, line 144
				{
					v_5 = limit - cursor;
					lab10: do {
						// literal, line 144
						if (!(eq_s_b("\u0BC8"))) {
							break lab10;
						}
						break lab9;
					} while (false);
					cursor = limit - v_5;
				}
				cursor = limit - v_4;
				// ], line 144
				bra = cursor;
				// <-, line 144
				slice_from("\u0BAE\u0BCD");
				// ], line 144
				bra = cursor;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab11: do {
				// (, line 146
				// [, line 146
				ket = cursor;
				// or, line 146
				lab12: do {
					v_6 = limit - cursor;
					lab13: do {
						// literal, line 146
						if (!(eq_s_b("\u0BC1\u0B95\u0BCD"))) {
							break lab13;
						}
						break lab12;
					} while (false);
					cursor = limit - v_6;
					// literal, line 146
					if (!(eq_s_b("\u0BC1\u0B95\u0BCD\u0B95\u0BCD"))) {
						break lab11;
					}
				} while (false);
				// ], line 146
				bra = cursor;
				// <-, line 146
				slice_from("\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab14: do {
				// (, line 148
				// [, line 148
				ket = cursor;
				// literal, line 148
				if (!(eq_s_b("\u0BCD"))) {
					break lab14;
				}
				// among, line 148
				if (find_among_b(a_3) == 0) {
					break lab14;
				}
				// literal, line 148
				if (!(eq_s_b("\u0BCD"))) {
					break lab14;
				}
				// among, line 148
				if (find_among_b(a_4) == 0) {
					break lab14;
				}
				// ], line 148
				bra = cursor;
				// delete, line 148
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab15: do {
				// (, line 150
				// [, line 150
				ket = cursor;
				// literal, line 150
				if (!(eq_s_b("\u0BC1\u0B95\u0BCD"))) {
					break lab15;
				}
				// ], line 150
				bra = cursor;
				// <-, line 150
				slice_from("\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab16: do {
				// (, line 152
				// [, line 152
				ket = cursor;
				// literal, line 152
				if (!(eq_s_b("\u0BCD"))) {
					break lab16;
				}
				// among, line 152
				if (find_among_b(a_5) == 0) {
					break lab16;
				}
				// ], line 152
				bra = cursor;
				// delete, line 152
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab17: do {
				// (, line 154
				// [, line 154
				ket = cursor;
				// literal, line 154
				if (!(eq_s_b("\u0BCD"))) {
					break lab17;
				}
				// (, line 154
				// or, line 154
				lab18: do {
					v_7 = limit - cursor;
					lab19: do {
						// among, line 154
						if (find_among_b(a_6) == 0) {
							break lab19;
						}
						break lab18;
					} while (false);
					cursor = limit - v_7;
					// among, line 154
					if (find_among_b(a_7) == 0) {
						break lab17;
					}
				} while (false);
				// literal, line 154
				if (!(eq_s_b("\u0BCD"))) {
					break lab17;
				}
				// ], line 154
				bra = cursor;
				// <-, line 154
				slice_from("\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab20: do {
				// (, line 156
				// [, line 156
				ket = cursor;
				// among, line 156
				if (find_among_b(a_8) == 0) {
					break lab20;
				}
				// ], line 156
				bra = cursor;
				// delete, line 156
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab21: do {
				// (, line 158
				// [, line 158
				ket = cursor;
				// literal, line 158
				if (!(eq_s_b("\u0BA9\u0BC1"))) {
					break lab21;
				}
				// (, line 158
				// test, line 158
				v_8 = limit - cursor;
				// not, line 158
				{
					v_9 = limit - cursor;
					lab22: do {
						// among, line 158
						if (find_among_b(a_9) == 0) {
							break lab22;
						}
						break lab21;
					} while (false);
					cursor = limit - v_9;
				}
				cursor = limit - v_8;
				// ], line 158
				bra = cursor;
				// delete, line 158
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab23: do {
				// (, line 160
				// [, line 160
				ket = cursor;
				// literal, line 160
				if (!(eq_s_b("\u0B99\u0BCD"))) {
					break lab23;
				}
				// (, line 160
				// test, line 160
				v_10 = limit - cursor;
				// not, line 160
				{
					v_11 = limit - cursor;
					lab24: do {
						// literal, line 160
						if (!(eq_s_b("\u0BC8"))) {
							break lab24;
						}
						break lab23;
					} while (false);
					cursor = limit - v_11;
				}
				cursor = limit - v_10;
				// ], line 160
				bra = cursor;
				// <-, line 160
				slice_from("\u0BAE\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab25: do {
				// (, line 162
				// [, line 162
				ket = cursor;
				// literal, line 162
				if (!(eq_s_b("\u0B99\u0BCD"))) {
					break lab25;
				}
				// ], line 162
				bra = cursor;
				// delete, line 162
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 164
			// [, line 164
			ket = cursor;
			// literal, line 164
			if (!(eq_s_b("\u0BCD"))) {
				return false;
			}
			// (, line 164
			// test, line 164
			v_12 = limit - cursor;
			// (, line 164
			// or, line 164
			lab26: do {
				v_13 = limit - cursor;
				lab27: do {
					// among, line 164
					if (find_among_b(a_10) == 0) {
						break lab27;
					}
					break lab26;
				} while (false);
				cursor = limit - v_13;
				// literal, line 164
				if (!(eq_s_b("\u0BCD"))) {
					return false;
				}
			} while (false);
			cursor = limit - v_12;
			// ], line 164
			bra = cursor;
			// delete, line 164
			slice_del();
		} while (false);
		cursor = limit_backward; // set found_wrong_ending, line 167
		B_found_wrong_ending = true;
		return true;
	}

	private boolean r_remove_pronoun_prefixes() {
		int v_1;
		// (, line 170
		// unset found_a_match, line 171
		B_found_a_match = false;
		// [, line 172
		bra = cursor;
		// among, line 172
		if (find_among(a_11) == 0) {
			return false;
		}
		// among, line 172
		if (find_among(a_12) == 0) {
			return false;
		}
		// literal, line 172
		if (!(eq_s("\u0BCD"))) {
			return false;
		}
		// ], line 172
		ket = cursor;
		// delete, line 172
		slice_del();
		// (, line 173
		// set found_a_match, line 173
		B_found_a_match = true;
		// do, line 174
		v_1 = cursor;
		lab0: do {
			// call fix_va_start, line 174
			if (!r_fix_va_start()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_remove_plural_suffix() {
		int v_1;
		int v_2;
		int v_3;
		// (, line 177
		// unset found_a_match, line 178
		B_found_a_match = false;
		// backwards, line 179
		limit_backward = cursor;
		cursor = limit;
		// (, line 179
		// or, line 180
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 180
				// [, line 180
				ket = cursor;
				// literal, line 180
				if (!(eq_s_b("\u0BC1\u0B99\u0BCD\u0B95\u0BB3\u0BCD"))) {
					break lab1;
				}
				// (, line 180
				// test, line 180
				v_2 = limit - cursor;
				// not, line 180
				{
					v_3 = limit - cursor;
					lab2: do {
						// among, line 180
						if (find_among_b(a_13) == 0) {
							break lab2;
						}
						break lab1;
					} while (false);
					cursor = limit - v_3;
				}
				cursor = limit - v_2;
				// ], line 180
				bra = cursor;
				// <-, line 180
				slice_from("\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab3: do {
				// (, line 181
				// [, line 181
				ket = cursor;
				// literal, line 181
				if (!(eq_s_b("\u0BB1\u0BCD\u0B95\u0BB3\u0BCD"))) {
					break lab3;
				}
				// ], line 181
				bra = cursor;
				// <-, line 181
				slice_from("\u0BB2\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab4: do {
				// (, line 182
				// [, line 182
				ket = cursor;
				// literal, line 182
				if (!(eq_s_b("\u0B9F\u0BCD\u0B95\u0BB3\u0BCD"))) {
					break lab4;
				}
				// ], line 182
				bra = cursor;
				// <-, line 182
				slice_from("\u0BB3\u0BCD");
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 183
			// [, line 183
			ket = cursor;
			// literal, line 183
			if (!(eq_s_b("\u0B95\u0BB3\u0BCD"))) {
				return false;
			}
			// ], line 183
			bra = cursor;
			// delete, line 183
			slice_del();
		} while (false);
		// (, line 184
		// set found_a_match, line 184
		B_found_a_match = true;
		cursor = limit_backward;
		return true;
	}

	private boolean r_remove_question_suffixes() {
		int v_1;
		int v_2;
		// (, line 188
		// call has_min_length, line 189
		if (!r_has_min_length()) {
			return false;
		}
		// unset found_a_match, line 190
		B_found_a_match = false;
		// backwards, line 191
		limit_backward = cursor;
		cursor = limit;
		// (, line 191
		// do, line 192
		v_1 = limit - cursor;
		lab0: do {
			// (, line 192
			// [, line 193
			ket = cursor;
			// among, line 193
			if (find_among_b(a_14) == 0) {
				break lab0;
			}
			// ], line 193
			bra = cursor;
			// <-, line 193
			slice_from("\u0BCD");
			// (, line 194
			// set found_a_match, line 194
			B_found_a_match = true;
		} while (false);
		cursor = limit - v_1;
		cursor = limit_backward; // do, line 197
		v_2 = cursor;
		lab1: do {
			// call fix_endings, line 197
			if (!r_fix_endings()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		return true;
	}

	private boolean r_remove_command_suffixes() {
		// (, line 200
		// call has_min_length, line 201
		if (!r_has_min_length()) {
			return false;
		}
		// unset found_a_match, line 202
		B_found_a_match = false;
		// backwards, line 203
		limit_backward = cursor;
		cursor = limit;
		// (, line 203
		// [, line 204
		ket = cursor;
		// among, line 204
		if (find_among_b(a_15) == 0) {
			return false;
		}
		// ], line 204
		bra = cursor;
		// delete, line 204
		slice_del();
		// (, line 205
		// set found_a_match, line 205
		B_found_a_match = true;
		cursor = limit_backward;
		return true;
	}

	private boolean r_remove_um() {
		int v_1;
		// (, line 209
		// unset found_a_match, line 210
		B_found_a_match = false;
		// call has_min_length, line 211
		if (!r_has_min_length()) {
			return false;
		}
		// backwards, line 212
		limit_backward = cursor;
		cursor = limit;
		// (, line 212
		// [, line 212
		ket = cursor;
		// literal, line 212
		if (!(eq_s_b("\u0BC1\u0BAE\u0BCD"))) {
			return false;
		}
		// ], line 212
		bra = cursor;
		// <-, line 212
		slice_from("\u0BCD");
		// (, line 213
		// set found_a_match, line 213
		B_found_a_match = true;
		cursor = limit_backward; // do, line 215
		v_1 = cursor;
		lab0: do {
			// call fix_ending, line 215
			if (!r_fix_ending()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		return true;
	}

	private boolean r_remove_common_word_endings() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		// (, line 218
		// unset found_a_match, line 222
		B_found_a_match = false;
		// call has_min_length, line 223
		if (!r_has_min_length()) {
			return false;
		}
		// backwards, line 224
		limit_backward = cursor;
		cursor = limit;
		// (, line 224
		// or, line 241
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// test, line 225
				v_2 = limit - cursor;
				// (, line 225
				// [, line 225
				ket = cursor;
				// or, line 225
				lab2: do {
					v_3 = limit - cursor;
					lab3: do {
						// literal, line 225
						if (!(eq_s_b("\u0BC1\u0B9F\u0BA9\u0BCD"))) {
							break lab3;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab4: do {
						// literal, line 226
						if (!(eq_s_b("\u0BBF\u0BB2\u0BCD\u0BB2\u0BC8"))) {
							break lab4;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab5: do {
						// literal, line 227
						if (!(eq_s_b("\u0BBF\u0B9F\u0BAE\u0BCD"))) {
							break lab5;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab6: do {
						// literal, line 228
						if (!(eq_s_b("\u0BBF\u0BA9\u0BCD\u0BB1\u0BBF"))) {
							break lab6;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab7: do {
						// literal, line 229
						if (!(eq_s_b("\u0BBE\u0B95\u0BBF"))) {
							break lab7;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab8: do {
						// literal, line 230
						if (!(eq_s_b("\u0BBE\u0B95\u0BBF\u0BAF"))) {
							break lab8;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab9: do {
						// literal, line 231
						if (!(eq_s_b("\u0BC6\u0BA9\u0BCD\u0BB1\u0BC1"))) {
							break lab9;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab10: do {
						// literal, line 232
						if (!(eq_s_b("\u0BC1\u0BB3\u0BCD\u0BB3"))) {
							break lab10;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab11: do {
						// literal, line 233
						if (!(eq_s_b("\u0BC1\u0B9F\u0BC8\u0BAF"))) {
							break lab11;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab12: do {
						// literal, line 234
						if (!(eq_s_b("\u0BC1\u0B9F\u0BC8"))) {
							break lab12;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab13: do {
						// literal, line 235
						if (!(eq_s_b("\u0BC6\u0BA9\u0BC1\u0BAE\u0BCD"))) {
							break lab13;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab14: do {
						// (, line 236
						// literal, line 236
						if (!(eq_s_b("\u0BB2\u0BCD\u0BB2"))) {
							break lab14;
						}
						// test, line 236
						v_4 = limit - cursor;
						// (, line 236
						// not, line 236
						{
							v_5 = limit - cursor;
							lab15: do {
								// among, line 236
								if (find_among_b(a_16) == 0) {
									break lab15;
								}
								break lab14;
							} while (false);
							cursor = limit - v_5;
						}
						cursor = limit - v_4;
						break lab2;
					} while (false);
					cursor = limit - v_3;
					lab16: do {
						// literal, line 237
						if (!(eq_s_b("\u0BC6\u0BA9"))) {
							break lab16;
						}
						break lab2;
					} while (false);
					cursor = limit - v_3;
					// literal, line 238
					if (!(eq_s_b("\u0BBE\u0B95\u0BBF"))) {
						break lab1;
					}
				} while (false);
				// ], line 238
				bra = cursor;
				// <-, line 238
				slice_from("\u0BCD");
				// (, line 239
				// set found_a_match, line 239
				B_found_a_match = true;
				cursor = limit - v_2;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// test, line 242
			v_6 = limit - cursor;
			// (, line 242
			// [, line 242
			ket = cursor;
			// among, line 242
			if (find_among_b(a_17) == 0) {
				return false;
			}
			// ], line 255
			bra = cursor;
			// delete, line 255
			slice_del();
			// (, line 256
			// set found_a_match, line 256
			B_found_a_match = true;
			cursor = limit - v_6;
		} while (false);
		cursor = limit_backward; // do, line 259
		v_7 = cursor;
		lab17: do {
			// call fix_endings, line 259
			if (!r_fix_endings()) {
				break lab17;
			}
		} while (false);
		cursor = v_7;
		return true;
	}

	private boolean r_remove_vetrumai_urupukal() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		int v_14;
		int v_15;
		int v_16;
		int v_17;
		int v_18;
		int v_19;
		int v_20;
		int v_21;
		// (, line 262
		// unset found_a_match, line 263
		B_found_a_match = false;
		// unset found_vetrumai_urupu, line 264
		B_found_vetrumai_urupu = false;
		// call has_min_length, line 265
		if (!r_has_min_length()) {
			return false;
		}
		// backwards, line 266
		limit_backward = cursor;
		cursor = limit;
		// (, line 266
		// (, line 267
		// or, line 269
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// test, line 268
				v_2 = limit - cursor;
				// (, line 268
				// [, line 268
				ket = cursor;
				// literal, line 268
				if (!(eq_s_b("\u0BA9\u0BC8"))) {
					break lab1;
				}
				// ], line 268
				bra = cursor;
				// delete, line 268
				slice_del();
				cursor = limit - v_2;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab2: do {
				// test, line 270
				v_3 = limit - cursor;
				// (, line 270
				// [, line 270
				ket = cursor;
				// or, line 271
				lab3: do {
					v_4 = limit - cursor;
					lab4: do {
						// (, line 270
						// or, line 270
						lab5: do {
							v_5 = limit - cursor;
							lab6: do {
								// literal, line 270
								if (!(eq_s_b("\u0BBF\u0BA9\u0BC8"))) {
									break lab6;
								}
								break lab5;
							} while (false);
							cursor = limit - v_5;
							// literal, line 271
							if (!(eq_s_b("\u0BC8"))) {
								break lab4;
							}
						} while (false);
						// (, line 271
						// test, line 271
						v_6 = limit - cursor;
						// not, line 271
						{
							v_7 = limit - cursor;
							lab7: do {
								// among, line 271
								if (find_among_b(a_18) == 0) {
									break lab7;
								}
								break lab4;
							} while (false);
							cursor = limit - v_7;
						}
						cursor = limit - v_6;
						break lab3;
					} while (false);
					cursor = limit - v_4;
					// (, line 272
					// literal, line 272
					if (!(eq_s_b("\u0BC8"))) {
						break lab2;
					}
					// (, line 272
					// test, line 272
					v_8 = limit - cursor;
					// (, line 272
					// among, line 272
					if (find_among_b(a_19) == 0) {
						break lab2;
					}
					// literal, line 272
					if (!(eq_s_b("\u0BCD"))) {
						break lab2;
					}
					cursor = limit - v_8;
				} while (false);
				// ], line 273
				bra = cursor;
				// <-, line 273
				slice_from("\u0BCD");
				cursor = limit - v_3;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab8: do {
				// test, line 276
				v_9 = limit - cursor;
				// (, line 276
				// [, line 276
				ket = cursor;
				// or, line 277
				lab9: do {
					v_10 = limit - cursor;
					lab10: do {
						// literal, line 277
						if (!(eq_s_b("\u0BCA\u0B9F\u0BC1"))) {
							break lab10;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab11: do {
						// literal, line 278
						if (!(eq_s_b("\u0BCB\u0B9F\u0BC1"))) {
							break lab11;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab12: do {
						// literal, line 279
						if (!(eq_s_b("\u0BBF\u0BB2\u0BCD"))) {
							break lab12;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab13: do {
						// literal, line 280
						if (!(eq_s_b("\u0BBF\u0BB1\u0BCD"))) {
							break lab13;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab14: do {
						// (, line 281
						// literal, line 281
						if (!(eq_s_b("\u0BBF\u0BA9\u0BCD"))) {
							break lab14;
						}
						// (, line 281
						// test, line 281
						v_11 = limit - cursor;
						// not, line 281
						{
							v_12 = limit - cursor;
							lab15: do {
								// literal, line 281
								if (!(eq_s_b("\u0BAE"))) {
									break lab15;
								}
								break lab14;
							} while (false);
							cursor = limit - v_12;
						}
						cursor = limit - v_11;
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab16: do {
						// literal, line 282
						if (!(eq_s_b("\u0BBF\u0BA9\u0BCD\u0BB1\u0BC1"))) {
							break lab16;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab17: do {
						// literal, line 283
						if (!(eq_s_b("\u0BBF\u0BB0\u0BC1\u0BA8\u0BCD\u0BA4\u0BC1"))) {
							break lab17;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab18: do {
						// literal, line 284
						if (!(eq_s_b("\u0BB5\u0BBF\u0B9F"))) {
							break lab18;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab19: do {
						// (, line 285
						if (!(I_length >= 7)) {
							break lab19;
						}
						// literal, line 285
						if (!(eq_s_b("\u0BBF\u0B9F\u0BAE\u0BCD"))) {
							break lab19;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab20: do {
						// literal, line 286
						if (!(eq_s_b("\u0BBE\u0BB2\u0BCD"))) {
							break lab20;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab21: do {
						// literal, line 287
						if (!(eq_s_b("\u0BC1\u0B9F\u0BC8"))) {
							break lab21;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab22: do {
						// literal, line 288
						if (!(eq_s_b("\u0BBE\u0BAE\u0BB2\u0BCD"))) {
							break lab22;
						}
						break lab9;
					} while (false);
					cursor = limit - v_10;
					lab23: do {
						// (, line 289
						// literal, line 289
						if (!(eq_s_b("\u0BB2\u0BCD"))) {
							break lab23;
						}
						// (, line 289
						// test, line 289
						v_13 = limit - cursor;
						// not, line 289
						{
							v_14 = limit - cursor;
							lab24: do {
								// among, line 289
								if (find_among_b(a_20) == 0) {
									break lab24;
								}
								break lab23;
							} while (false);
							cursor = limit - v_14;
						}
						cursor = limit - v_13;
						break lab9;
					} while (false);
					cursor = limit - v_10;
					// literal, line 290
					if (!(eq_s_b("\u0BC1\u0BB3\u0BCD"))) {
						break lab8;
					}
				} while (false);
				// ], line 291
				bra = cursor;
				// <-, line 291
				slice_from("\u0BCD");
				cursor = limit - v_9;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab25: do {
				// test, line 294
				v_15 = limit - cursor;
				// (, line 294
				// [, line 294
				ket = cursor;
				// or, line 295
				lab26: do {
					v_16 = limit - cursor;
					lab27: do {
						// literal, line 295
						if (!(eq_s_b("\u0B95\u0BA3\u0BCD"))) {
							break lab27;
						}
						break lab26;
					} while (false);
					cursor = limit - v_16;
					lab28: do {
						// literal, line 296
						if (!(eq_s_b("\u0BAE\u0BC1\u0BA9\u0BCD"))) {
							break lab28;
						}
						break lab26;
					} while (false);
					cursor = limit - v_16;
					lab29: do {
						// literal, line 297
						if (!(eq_s_b("\u0BAE\u0BC7\u0BB2\u0BCD"))) {
							break lab29;
						}
						break lab26;
					} while (false);
					cursor = limit - v_16;
					lab30: do {
						// literal, line 298
						if (!(eq_s_b("\u0BAE\u0BC7\u0BB1\u0BCD"))) {
							break lab30;
						}
						break lab26;
					} while (false);
					cursor = limit - v_16;
					lab31: do {
						// literal, line 299
						if (!(eq_s_b("\u0B95\u0BC0\u0BB4\u0BCD"))) {
							break lab31;
						}
						break lab26;
					} while (false);
					cursor = limit - v_16;
					lab32: do {
						// literal, line 300
						if (!(eq_s_b("\u0BAA\u0BBF\u0BA9\u0BCD"))) {
							break lab32;
						}
						break lab26;
					} while (false);
					cursor = limit - v_16;
					// (, line 301
					// literal, line 301
					if (!(eq_s_b("\u0BA4\u0BC1"))) {
						break lab25;
					}
					// (, line 301
					// test, line 301
					v_17 = limit - cursor;
					// not, line 301
					{
						v_18 = limit - cursor;
						lab33: do {
							// among, line 301
							if (find_among_b(a_21) == 0) {
								break lab33;
							}
							break lab25;
						} while (false);
						cursor = limit - v_18;
					}
					cursor = limit - v_17;
				} while (false);
				// ], line 302
				bra = cursor;
				// delete, line 302
				slice_del();
				cursor = limit - v_15;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// test, line 305
			v_19 = limit - cursor;
			// (, line 305
			// [, line 305
			ket = cursor;
			// literal, line 305
			if (!(eq_s_b("\u0BC0"))) {
				return false;
			}
			// ], line 305
			bra = cursor;
			// <-, line 305
			slice_from("\u0BBF");
			cursor = limit - v_19;
		} while (false);
		// (, line 307
		// set found_a_match, line 307
		B_found_a_match = true;
		// (, line 308
		// set found_vetrumai_urupu, line 308
		B_found_vetrumai_urupu = true;
		// do, line 309
		v_20 = limit - cursor;
		lab34: do {
			// (, line 309
			// [, line 309
			ket = cursor;
			// literal, line 309
			if (!(eq_s_b("\u0BBF\u0BA9\u0BCD"))) {
				break lab34;
			}
			// ], line 309
			bra = cursor;
			// <-, line 309
			slice_from("\u0BCD");
		} while (false);
		cursor = limit - v_20;
		cursor = limit_backward; // do, line 311
		v_21 = cursor;
		lab35: do {
			// call fix_endings, line 311
			if (!r_fix_endings()) {
				break lab35;
			}
		} while (false);
		cursor = v_21;
		return true;
	}

	private boolean r_remove_tense_suffixes() {
		int v_1;
		int v_2;
		// (, line 314
		// set found_a_match, line 315
		B_found_a_match = true;
		// repeat, line 316
		replab0: while (true) {
			v_1 = cursor;
			lab1: do {
				// (, line 316
				// Boolean test found_a_match, line 316
				if (!(B_found_a_match)) {
					break lab1;
				}
				// (, line 316
				// do, line 316
				v_2 = cursor;
				lab2: do {
					// call remove_tense_suffix, line 316
					if (!r_remove_tense_suffix()) {
						break lab2;
					}
				} while (false);
				cursor = v_2;
				continue replab0;
			} while (false);
			cursor = v_1;
			break replab0;
		}
		return true;
	}

	private boolean r_remove_tense_suffix() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		int v_14;
		int v_15;
		int v_16;
		int v_17;
		int v_18;
		// (, line 319
		// unset found_a_match, line 320
		B_found_a_match = false;
		// call has_min_length, line 321
		if (!r_has_min_length()) {
			return false;
		}
		// backwards, line 322
		limit_backward = cursor;
		cursor = limit;
		// (, line 322
		// do, line 323
		v_1 = limit - cursor;
		lab0: do {
			// (, line 323
			// or, line 330
			lab1: do {
				v_2 = limit - cursor;
				lab2: do {
					// test, line 324
					v_3 = limit - cursor;
					// (, line 324
					// [, line 324
					ket = cursor;
					// among, line 324
					if (find_among_b(a_22) == 0) {
						break lab2;
					}
					// ], line 327
					bra = cursor;
					// delete, line 327
					slice_del();
					// (, line 328
					// set found_a_match, line 328
					B_found_a_match = true;
					cursor = limit - v_3;
					break lab1;
				} while (false);
				cursor = limit - v_2;
				lab3: do {
					// test, line 331
					v_4 = limit - cursor;
					// (, line 331
					// [, line 331
					ket = cursor;
					// or, line 332
					lab4: do {
						v_5 = limit - cursor;
						lab5: do {
							// literal, line 332
							if (!(eq_s_b("\u0BAE\u0BBE\u0BB0\u0BCD"))) {
								break lab5;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab6: do {
							// literal, line 333
							if (!(eq_s_b("\u0BAE\u0BBF\u0BA9\u0BCD"))) {
								break lab6;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab7: do {
							// literal, line 334
							if (!(eq_s_b("\u0BA9\u0BA9\u0BCD"))) {
								break lab7;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab8: do {
							// literal, line 335
							if (!(eq_s_b("\u0BA9\u0BBE\u0BA9\u0BCD"))) {
								break lab8;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab9: do {
							// literal, line 336
							if (!(eq_s_b("\u0BA9\u0BBE\u0BB3\u0BCD"))) {
								break lab9;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab10: do {
							// literal, line 337
							if (!(eq_s_b("\u0BA9\u0BBE\u0BB0\u0BCD"))) {
								break lab10;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab11: do {
							// (, line 338
							// literal, line 338
							if (!(eq_s_b("\u0BB5\u0BA9\u0BCD"))) {
								break lab11;
							}
							// test, line 338
							v_6 = limit - cursor;
							// (, line 338
							// not, line 338
							{
								v_7 = limit - cursor;
								lab12: do {
									// among, line 338
									if (find_among_b(a_23) == 0) {
										break lab12;
									}
									break lab11;
								} while (false);
								cursor = limit - v_7;
							}
							cursor = limit - v_6;
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab13: do {
							// literal, line 339
							if (!(eq_s_b("\u0BA9\u0BB3\u0BCD"))) {
								break lab13;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab14: do {
							// literal, line 340
							if (!(eq_s_b("\u0BB5\u0BB3\u0BCD"))) {
								break lab14;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab15: do {
							// literal, line 341
							if (!(eq_s_b("\u0BA9\u0BB0\u0BCD"))) {
								break lab15;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab16: do {
							// literal, line 342
							if (!(eq_s_b("\u0BB5\u0BB0\u0BCD"))) {
								break lab16;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab17: do {
							// literal, line 343
							if (!(eq_s_b("\u0BA9"))) {
								break lab17;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab18: do {
							// literal, line 343
							if (!(eq_s_b("\u0BAA"))) {
								break lab18;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab19: do {
							// literal, line 343
							if (!(eq_s_b("\u0B95"))) {
								break lab19;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab20: do {
							// literal, line 343
							if (!(eq_s_b("\u0BA4"))) {
								break lab20;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab21: do {
							// literal, line 343
							if (!(eq_s_b("\u0BAF"))) {
								break lab21;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab22: do {
							// literal, line 344
							if (!(eq_s_b("\u0BAA\u0BA9\u0BCD"))) {
								break lab22;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab23: do {
							// literal, line 345
							if (!(eq_s_b("\u0BAA\u0BB3\u0BCD"))) {
								break lab23;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab24: do {
							// literal, line 346
							if (!(eq_s_b("\u0BAA\u0BB0\u0BCD"))) {
								break lab24;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab25: do {
							// (, line 347
							// literal, line 347
							if (!(eq_s_b("\u0BA4\u0BC1"))) {
								break lab25;
							}
							// (, line 347
							// test, line 347
							v_8 = limit - cursor;
							// not, line 347
							{
								v_9 = limit - cursor;
								lab26: do {
									// among, line 347
									if (find_among_b(a_24) == 0) {
										break lab26;
									}
									break lab25;
								} while (false);
								cursor = limit - v_9;
							}
							cursor = limit - v_8;
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab27: do {
							// literal, line 348
							if (!(eq_s_b("\u0BBF\u0BB1\u0BCD\u0BB1\u0BC1"))) {
								break lab27;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab28: do {
							// literal, line 349
							if (!(eq_s_b("\u0BAA\u0BAE\u0BCD"))) {
								break lab28;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab29: do {
							// literal, line 350
							if (!(eq_s_b("\u0BA9\u0BAE\u0BCD"))) {
								break lab29;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab30: do {
							// literal, line 351
							if (!(eq_s_b("\u0BA4\u0BC1\u0BAE\u0BCD"))) {
								break lab30;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab31: do {
							// literal, line 352
							if (!(eq_s_b("\u0BB1\u0BC1\u0BAE\u0BCD"))) {
								break lab31;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab32: do {
							// literal, line 353
							if (!(eq_s_b("\u0B95\u0BC1\u0BAE\u0BCD"))) {
								break lab32;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab33: do {
							// literal, line 354
							if (!(eq_s_b("\u0BA9\u0BC6\u0BA9\u0BCD"))) {
								break lab33;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						lab34: do {
							// literal, line 355
							if (!(eq_s_b("\u0BA9\u0BC8"))) {
								break lab34;
							}
							break lab4;
						} while (false);
						cursor = limit - v_5;
						// literal, line 356
						if (!(eq_s_b("\u0BB5\u0BC8"))) {
							break lab3;
						}
					} while (false);
					// ], line 357
					bra = cursor;
					// delete, line 357
					slice_del();
					// (, line 358
					// set found_a_match, line 358
					B_found_a_match = true;
					cursor = limit - v_4;
					break lab1;
				} while (false);
				cursor = limit - v_2;
				lab35: do {
					// test, line 361
					v_10 = limit - cursor;
					// (, line 361
					// [, line 361
					ket = cursor;
					// or, line 362
					lab36: do {
						v_11 = limit - cursor;
						lab37: do {
							// (, line 362
							// literal, line 362
							if (!(eq_s_b("\u0BBE\u0BA9\u0BCD"))) {
								break lab37;
							}
							// test, line 362
							v_12 = limit - cursor;
							// (, line 362
							// not, line 362
							{
								v_13 = limit - cursor;
								lab38: do {
									// literal, line 362
									if (!(eq_s_b("\u0B9A"))) {
										break lab38;
									}
									break lab37;
								} while (false);
								cursor = limit - v_13;
							}
							cursor = limit - v_12;
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab39: do {
							// literal, line 363
							if (!(eq_s_b("\u0BBE\u0BB3\u0BCD"))) {
								break lab39;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab40: do {
							// literal, line 364
							if (!(eq_s_b("\u0BBE\u0BB0\u0BCD"))) {
								break lab40;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab41: do {
							// literal, line 365
							if (!(eq_s_b("\u0BC7\u0BA9\u0BCD"))) {
								break lab41;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab42: do {
							// literal, line 366
							if (!(eq_s_b("\u0BBE"))) {
								break lab42;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab43: do {
							// literal, line 367
							if (!(eq_s_b("\u0BBE\u0BAE\u0BCD"))) {
								break lab43;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab44: do {
							// literal, line 368
							if (!(eq_s_b("\u0BC6\u0BAE\u0BCD"))) {
								break lab44;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab45: do {
							// literal, line 369
							if (!(eq_s_b("\u0BC7\u0BAE\u0BCD"))) {
								break lab45;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab46: do {
							// literal, line 370
							if (!(eq_s_b("\u0BCB\u0BAE\u0BCD"))) {
								break lab46;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab47: do {
							// literal, line 371
							if (!(eq_s_b("\u0B95\u0BC1\u0BAE\u0BCD"))) {
								break lab47;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab48: do {
							// literal, line 372
							if (!(eq_s_b("\u0BA4\u0BC1\u0BAE\u0BCD"))) {
								break lab48;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab49: do {
							// literal, line 373
							if (!(eq_s_b("\u0B9F\u0BC1\u0BAE\u0BCD"))) {
								break lab49;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab50: do {
							// literal, line 374
							if (!(eq_s_b("\u0BB1\u0BC1\u0BAE\u0BCD"))) {
								break lab50;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab51: do {
							// literal, line 375
							if (!(eq_s_b("\u0BBE\u0BAF\u0BCD"))) {
								break lab51;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab52: do {
							// literal, line 376
							if (!(eq_s_b("\u0BA9\u0BC6\u0BA9\u0BCD"))) {
								break lab52;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab53: do {
							// literal, line 377
							if (!(eq_s_b("\u0BA9\u0BBF\u0BB0\u0BCD"))) {
								break lab53;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						lab54: do {
							// literal, line 378
							if (!(eq_s_b("\u0BC0\u0BB0\u0BCD"))) {
								break lab54;
							}
							break lab36;
						} while (false);
						cursor = limit - v_11;
						// literal, line 379
						if (!(eq_s_b("\u0BC0\u0BAF\u0BB0\u0BCD"))) {
							break lab35;
						}
					} while (false);
					// ], line 380
					bra = cursor;
					// <-, line 380
					slice_from("\u0BCD");
					// (, line 381
					// set found_a_match, line 381
					B_found_a_match = true;
					cursor = limit - v_10;
					break lab1;
				} while (false);
				cursor = limit - v_2;
				// test, line 384
				v_14 = limit - cursor;
				// (, line 384
				// (, line 384
				// [, line 384
				ket = cursor;
				// or, line 384
				lab55: do {
					v_15 = limit - cursor;
					lab56: do {
						// literal, line 384
						if (!(eq_s_b("\u0B95\u0BC1"))) {
							break lab56;
						}
						break lab55;
					} while (false);
					cursor = limit - v_15;
					// literal, line 384
					if (!(eq_s_b("\u0BA4\u0BC1"))) {
						break lab0;
					}
				} while (false);
				// (, line 384
				// test, line 384
				v_16 = limit - cursor;
				// literal, line 384
				if (!(eq_s_b("\u0BCD"))) {
					break lab0;
				}
				cursor = limit - v_16;
				// ], line 384
				bra = cursor;
				// delete, line 384
				slice_del();
				// (, line 385
				// set found_a_match, line 385
				B_found_a_match = true;
				cursor = limit - v_14;
			} while (false);
		} while (false);
		cursor = limit - v_1;
		// do, line 388
		v_17 = limit - cursor;
		lab57: do {
			// (, line 388
			// [, line 388
			ket = cursor;
			// among, line 388
			if (find_among_b(a_25) == 0) {
				break lab57;
			}
			// ], line 395
			bra = cursor;
			// delete, line 395
			slice_del();
			// (, line 396
			// set found_a_match, line 396
			B_found_a_match = true;
		} while (false);
		cursor = limit - v_17;
		cursor = limit_backward; // do, line 399
		v_18 = cursor;
		lab58: do {
			// call fix_endings, line 399
			if (!r_fix_endings()) {
				break lab58;
			}
		} while (false);
		cursor = v_18;
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 402
		// unset found_vetrumai_urupu, line 403
		B_found_vetrumai_urupu = false;
		// do, line 404
		v_1 = cursor;
		lab0: do {
			// call fix_ending, line 404
			if (!r_fix_ending()) {
				break lab0;
			}
		} while (false);
		cursor = v_1;
		// call has_min_length, line 405
		if (!r_has_min_length()) {
			return false;
		}
		// do, line 406
		v_2 = cursor;
		lab1: do {
			// call remove_question_prefixes, line 406
			if (!r_remove_question_prefixes()) {
				break lab1;
			}
		} while (false);
		cursor = v_2;
		// do, line 407
		v_3 = cursor;
		lab2: do {
			// call remove_pronoun_prefixes, line 407
			if (!r_remove_pronoun_prefixes()) {
				break lab2;
			}
		} while (false);
		cursor = v_3;
		// do, line 408
		v_4 = cursor;
		lab3: do {
			// call remove_question_suffixes, line 408
			if (!r_remove_question_suffixes()) {
				break lab3;
			}
		} while (false);
		cursor = v_4;
		// do, line 409
		v_5 = cursor;
		lab4: do {
			// call remove_um, line 409
			if (!r_remove_um()) {
				break lab4;
			}
		} while (false);
		cursor = v_5;
		// do, line 410
		v_6 = cursor;
		lab5: do {
			// call remove_common_word_endings, line 410
			if (!r_remove_common_word_endings()) {
				break lab5;
			}
		} while (false);
		cursor = v_6;
		// do, line 411
		v_7 = cursor;
		lab6: do {
			// call remove_vetrumai_urupukal, line 411
			if (!r_remove_vetrumai_urupukal()) {
				break lab6;
			}
		} while (false);
		cursor = v_7;
		// do, line 412
		v_8 = cursor;
		lab7: do {
			// call remove_plural_suffix, line 412
			if (!r_remove_plural_suffix()) {
				break lab7;
			}
		} while (false);
		cursor = v_8;
		// do, line 413
		v_9 = cursor;
		lab8: do {
			// call remove_command_suffixes, line 413
			if (!r_remove_command_suffixes()) {
				break lab8;
			}
		} while (false);
		cursor = v_9;
		// do, line 414
		v_10 = cursor;
		lab9: do {
			// call remove_tense_suffixes, line 414
			if (!r_remove_tense_suffixes()) {
				break lab9;
			}
		} while (false);
		cursor = v_10;
		return true;
	}
}
// This file was generated automatically by the Snowball to Java compiler
// http://snowballstem.org/



/**
 * This class was automatically generated by a Snowball to Java compiler It
 * implements the stemming algorithm defined by a snowball script.
 */

public class turkishStemmer extends SnowballStemmer {

	private final static Among a_0[] = { new Among("m", -1, -1), new Among("n", -1, -1), new Among("miz", -1, -1),
			new Among("niz", -1, -1), new Among("muz", -1, -1), new Among("nuz", -1, -1), new Among("m\u00FCz", -1, -1),
			new Among("n\u00FCz", -1, -1), new Among("m\u0131z", -1, -1), new Among("n\u0131z", -1, -1) };

	private final static Among a_1[] = { new Among("leri", -1, -1), new Among("lar\u0131", -1, -1) };

	private final static Among a_2[] = { new Among("ni", -1, -1), new Among("nu", -1, -1), new Among("n\u00FC", -1, -1),
			new Among("n\u0131", -1, -1) };

	private final static Among a_3[] = { new Among("in", -1, -1), new Among("un", -1, -1), new Among("\u00FCn", -1, -1),
			new Among("\u0131n", -1, -1) };

	private final static Among a_4[] = { new Among("a", -1, -1), new Among("e", -1, -1) };

	private final static Among a_5[] = { new Among("na", -1, -1), new Among("ne", -1, -1) };

	private final static Among a_6[] = { new Among("da", -1, -1), new Among("ta", -1, -1), new Among("de", -1, -1),
			new Among("te", -1, -1) };

	private final static Among a_7[] = { new Among("nda", -1, -1), new Among("nde", -1, -1) };

	private final static Among a_8[] = { new Among("dan", -1, -1), new Among("tan", -1, -1), new Among("den", -1, -1),
			new Among("ten", -1, -1) };

	private final static Among a_9[] = { new Among("ndan", -1, -1), new Among("nden", -1, -1) };

	private final static Among a_10[] = { new Among("la", -1, -1), new Among("le", -1, -1) };

	private final static Among a_11[] = { new Among("ca", -1, -1), new Among("ce", -1, -1) };

	private final static Among a_12[] = { new Among("im", -1, -1), new Among("um", -1, -1),
			new Among("\u00FCm", -1, -1), new Among("\u0131m", -1, -1) };

	private final static Among a_13[] = { new Among("sin", -1, -1), new Among("sun", -1, -1),
			new Among("s\u00FCn", -1, -1), new Among("s\u0131n", -1, -1) };

	private final static Among a_14[] = { new Among("iz", -1, -1), new Among("uz", -1, -1),
			new Among("\u00FCz", -1, -1), new Among("\u0131z", -1, -1) };

	private final static Among a_15[] = { new Among("siniz", -1, -1), new Among("sunuz", -1, -1),
			new Among("s\u00FCn\u00FCz", -1, -1), new Among("s\u0131n\u0131z", -1, -1) };

	private final static Among a_16[] = { new Among("lar", -1, -1), new Among("ler", -1, -1) };

	private final static Among a_17[] = { new Among("niz", -1, -1), new Among("nuz", -1, -1),
			new Among("n\u00FCz", -1, -1), new Among("n\u0131z", -1, -1) };

	private final static Among a_18[] = { new Among("dir", -1, -1), new Among("tir", -1, -1), new Among("dur", -1, -1),
			new Among("tur", -1, -1), new Among("d\u00FCr", -1, -1), new Among("t\u00FCr", -1, -1),
			new Among("d\u0131r", -1, -1), new Among("t\u0131r", -1, -1) };

	private final static Among a_19[] = { new Among("cas\u0131na", -1, -1), new Among("cesine", -1, -1) };

	private final static Among a_20[] = { new Among("di", -1, -1), new Among("ti", -1, -1), new Among("dik", -1, -1),
			new Among("tik", -1, -1), new Among("duk", -1, -1), new Among("tuk", -1, -1), new Among("d\u00FCk", -1, -1),
			new Among("t\u00FCk", -1, -1), new Among("d\u0131k", -1, -1), new Among("t\u0131k", -1, -1),
			new Among("dim", -1, -1), new Among("tim", -1, -1), new Among("dum", -1, -1), new Among("tum", -1, -1),
			new Among("d\u00FCm", -1, -1), new Among("t\u00FCm", -1, -1), new Among("d\u0131m", -1, -1),
			new Among("t\u0131m", -1, -1), new Among("din", -1, -1), new Among("tin", -1, -1), new Among("dun", -1, -1),
			new Among("tun", -1, -1), new Among("d\u00FCn", -1, -1), new Among("t\u00FCn", -1, -1),
			new Among("d\u0131n", -1, -1), new Among("t\u0131n", -1, -1), new Among("du", -1, -1),
			new Among("tu", -1, -1), new Among("d\u00FC", -1, -1), new Among("t\u00FC", -1, -1),
			new Among("d\u0131", -1, -1), new Among("t\u0131", -1, -1) };

	private final static Among a_21[] = { new Among("sa", -1, -1), new Among("se", -1, -1), new Among("sak", -1, -1),
			new Among("sek", -1, -1), new Among("sam", -1, -1), new Among("sem", -1, -1), new Among("san", -1, -1),
			new Among("sen", -1, -1) };

	private final static Among a_22[] = { new Among("mi\u015F", -1, -1), new Among("mu\u015F", -1, -1),
			new Among("m\u00FC\u015F", -1, -1), new Among("m\u0131\u015F", -1, -1) };

	private final static Among a_23[] = { new Among("b", -1, 1), new Among("c", -1, 2), new Among("d", -1, 3),
			new Among("\u011F", -1, 4) };

	private static final char g_vowel[] = { 17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 8, 0, 0, 0, 0,
			0, 0, 1 };

	private static final char g_U[] = { 1, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 1 };

	private static final char g_vowel1[] = { 1, 64, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 1 };

	private static final char g_vowel2[] = { 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 130 };

	private static final char g_vowel3[] = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 1 };

	private static final char g_vowel4[] = { 17 };

	private static final char g_vowel5[] = { 65 };

	private static final char g_vowel6[] = { 65 };

	private boolean B_continue_stemming_noun_suffixes;
	private int I_strlen;

	private boolean r_check_vowel_harmony() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		// (, line 111
		// test, line 112
		v_1 = limit - cursor;
		// (, line 113
		// (, line 114
		// goto, line 114
		golab0: while (true) {
			v_2 = limit - cursor;
			lab1: do {
				if (!(in_grouping_b(g_vowel, 97, 305))) {
					break lab1;
				}
				cursor = limit - v_2;
				break golab0;
			} while (false);
			cursor = limit - v_2;
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
		}
		// (, line 115
		// or, line 116
		lab2: do {
			v_3 = limit - cursor;
			lab3: do {
				// (, line 116
				// literal, line 116
				if (!(eq_s_b("a"))) {
					break lab3;
				}
				// goto, line 116
				golab4: while (true) {
					v_4 = limit - cursor;
					lab5: do {
						if (!(in_grouping_b(g_vowel1, 97, 305))) {
							break lab5;
						}
						cursor = limit - v_4;
						break golab4;
					} while (false);
					cursor = limit - v_4;
					if (cursor <= limit_backward) {
						break lab3;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab6: do {
				// (, line 117
				// literal, line 117
				if (!(eq_s_b("e"))) {
					break lab6;
				}
				// goto, line 117
				golab7: while (true) {
					v_5 = limit - cursor;
					lab8: do {
						if (!(in_grouping_b(g_vowel2, 101, 252))) {
							break lab8;
						}
						cursor = limit - v_5;
						break golab7;
					} while (false);
					cursor = limit - v_5;
					if (cursor <= limit_backward) {
						break lab6;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab9: do {
				// (, line 118
				// literal, line 118
				if (!(eq_s_b("\u0131"))) {
					break lab9;
				}
				// goto, line 118
				golab10: while (true) {
					v_6 = limit - cursor;
					lab11: do {
						if (!(in_grouping_b(g_vowel3, 97, 305))) {
							break lab11;
						}
						cursor = limit - v_6;
						break golab10;
					} while (false);
					cursor = limit - v_6;
					if (cursor <= limit_backward) {
						break lab9;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab12: do {
				// (, line 119
				// literal, line 119
				if (!(eq_s_b("i"))) {
					break lab12;
				}
				// goto, line 119
				golab13: while (true) {
					v_7 = limit - cursor;
					lab14: do {
						if (!(in_grouping_b(g_vowel4, 101, 105))) {
							break lab14;
						}
						cursor = limit - v_7;
						break golab13;
					} while (false);
					cursor = limit - v_7;
					if (cursor <= limit_backward) {
						break lab12;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab15: do {
				// (, line 120
				// literal, line 120
				if (!(eq_s_b("o"))) {
					break lab15;
				}
				// goto, line 120
				golab16: while (true) {
					v_8 = limit - cursor;
					lab17: do {
						if (!(in_grouping_b(g_vowel5, 111, 117))) {
							break lab17;
						}
						cursor = limit - v_8;
						break golab16;
					} while (false);
					cursor = limit - v_8;
					if (cursor <= limit_backward) {
						break lab15;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab18: do {
				// (, line 121
				// literal, line 121
				if (!(eq_s_b("\u00F6"))) {
					break lab18;
				}
				// goto, line 121
				golab19: while (true) {
					v_9 = limit - cursor;
					lab20: do {
						if (!(in_grouping_b(g_vowel6, 246, 252))) {
							break lab20;
						}
						cursor = limit - v_9;
						break golab19;
					} while (false);
					cursor = limit - v_9;
					if (cursor <= limit_backward) {
						break lab18;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab21: do {
				// (, line 122
				// literal, line 122
				if (!(eq_s_b("u"))) {
					break lab21;
				}
				// goto, line 122
				golab22: while (true) {
					v_10 = limit - cursor;
					lab23: do {
						if (!(in_grouping_b(g_vowel5, 111, 117))) {
							break lab23;
						}
						cursor = limit - v_10;
						break golab22;
					} while (false);
					cursor = limit - v_10;
					if (cursor <= limit_backward) {
						break lab21;
					}
					cursor--;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			// (, line 123
			// literal, line 123
			if (!(eq_s_b("\u00FC"))) {
				return false;
			}
			// goto, line 123
			golab24: while (true) {
				v_11 = limit - cursor;
				lab25: do {
					if (!(in_grouping_b(g_vowel6, 246, 252))) {
						break lab25;
					}
					cursor = limit - v_11;
					break golab24;
				} while (false);
				cursor = limit - v_11;
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
			}
		} while (false);
		cursor = limit - v_1;
		return true;
	}

	private boolean r_mark_suffix_with_optional_n_consonant() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 132
		// or, line 134
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 133
				// literal, line 133
				if (!(eq_s_b("n"))) {
					break lab1;
				}
				// (, line 133
				// test, line 133
				v_2 = limit - cursor;
				if (!(in_grouping_b(g_vowel, 97, 305))) {
					break lab1;
				}
				cursor = limit - v_2;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 135
			// (, line 135
			// not, line 135
			{
				v_3 = limit - cursor;
				lab2: do {
					// (, line 135
					// test, line 135
					v_4 = limit - cursor;
					// literal, line 135
					if (!(eq_s_b("n"))) {
						break lab2;
					}
					cursor = limit - v_4;
					return false;
				} while (false);
				cursor = limit - v_3;
			}
			// test, line 135
			v_5 = limit - cursor;
			// (, line 135
			// next, line 135
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
			if (!(in_grouping_b(g_vowel, 97, 305))) {
				return false;
			}
			cursor = limit - v_5;
		} while (false);
		return true;
	}

	private boolean r_mark_suffix_with_optional_s_consonant() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 143
		// or, line 145
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 144
				// literal, line 144
				if (!(eq_s_b("s"))) {
					break lab1;
				}
				// (, line 144
				// test, line 144
				v_2 = limit - cursor;
				if (!(in_grouping_b(g_vowel, 97, 305))) {
					break lab1;
				}
				cursor = limit - v_2;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 146
			// (, line 146
			// not, line 146
			{
				v_3 = limit - cursor;
				lab2: do {
					// (, line 146
					// test, line 146
					v_4 = limit - cursor;
					// literal, line 146
					if (!(eq_s_b("s"))) {
						break lab2;
					}
					cursor = limit - v_4;
					return false;
				} while (false);
				cursor = limit - v_3;
			}
			// test, line 146
			v_5 = limit - cursor;
			// (, line 146
			// next, line 146
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
			if (!(in_grouping_b(g_vowel, 97, 305))) {
				return false;
			}
			cursor = limit - v_5;
		} while (false);
		return true;
	}

	private boolean r_mark_suffix_with_optional_y_consonant() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 153
		// or, line 155
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 154
				// literal, line 154
				if (!(eq_s_b("y"))) {
					break lab1;
				}
				// (, line 154
				// test, line 154
				v_2 = limit - cursor;
				if (!(in_grouping_b(g_vowel, 97, 305))) {
					break lab1;
				}
				cursor = limit - v_2;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 156
			// (, line 156
			// not, line 156
			{
				v_3 = limit - cursor;
				lab2: do {
					// (, line 156
					// test, line 156
					v_4 = limit - cursor;
					// literal, line 156
					if (!(eq_s_b("y"))) {
						break lab2;
					}
					cursor = limit - v_4;
					return false;
				} while (false);
				cursor = limit - v_3;
			}
			// test, line 156
			v_5 = limit - cursor;
			// (, line 156
			// next, line 156
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
			if (!(in_grouping_b(g_vowel, 97, 305))) {
				return false;
			}
			cursor = limit - v_5;
		} while (false);
		return true;
	}

	private boolean r_mark_suffix_with_optional_U_vowel() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		// (, line 159
		// or, line 161
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 160
				if (!(in_grouping_b(g_U, 105, 305))) {
					break lab1;
				}
				// (, line 160
				// test, line 160
				v_2 = limit - cursor;
				if (!(out_grouping_b(g_vowel, 97, 305))) {
					break lab1;
				}
				cursor = limit - v_2;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 162
			// (, line 162
			// not, line 162
			{
				v_3 = limit - cursor;
				lab2: do {
					// (, line 162
					// test, line 162
					v_4 = limit - cursor;
					if (!(in_grouping_b(g_U, 105, 305))) {
						break lab2;
					}
					cursor = limit - v_4;
					return false;
				} while (false);
				cursor = limit - v_3;
			}
			// test, line 162
			v_5 = limit - cursor;
			// (, line 162
			// next, line 162
			if (cursor <= limit_backward) {
				return false;
			}
			cursor--;
			if (!(out_grouping_b(g_vowel, 97, 305))) {
				return false;
			}
			cursor = limit - v_5;
		} while (false);
		return true;
	}

	private boolean r_mark_possessives() {
		// (, line 166
		// among, line 167
		if (find_among_b(a_0) == 0) {
			return false;
		}
		// (, line 169
		// call mark_suffix_with_optional_U_vowel, line 169
		if (!r_mark_suffix_with_optional_U_vowel()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_sU() {
		// (, line 172
		// call check_vowel_harmony, line 173
		if (!r_check_vowel_harmony()) {
			return false;
		}
		if (!(in_grouping_b(g_U, 105, 305))) {
			return false;
		}
		// (, line 175
		// call mark_suffix_with_optional_s_consonant, line 175
		if (!r_mark_suffix_with_optional_s_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_lArI() {
		// (, line 178
		// among, line 179
		if (find_among_b(a_1) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_yU() {
		// (, line 182
		// call check_vowel_harmony, line 183
		if (!r_check_vowel_harmony()) {
			return false;
		}
		if (!(in_grouping_b(g_U, 105, 305))) {
			return false;
		}
		// (, line 185
		// call mark_suffix_with_optional_y_consonant, line 185
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_nU() {
		// (, line 188
		// call check_vowel_harmony, line 189
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 190
		if (find_among_b(a_2) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_nUn() {
		// (, line 193
		// call check_vowel_harmony, line 194
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 195
		if (find_among_b(a_3) == 0) {
			return false;
		}
		// (, line 196
		// call mark_suffix_with_optional_n_consonant, line 196
		if (!r_mark_suffix_with_optional_n_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_yA() {
		// (, line 199
		// call check_vowel_harmony, line 200
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 201
		if (find_among_b(a_4) == 0) {
			return false;
		}
		// (, line 202
		// call mark_suffix_with_optional_y_consonant, line 202
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_nA() {
		// (, line 205
		// call check_vowel_harmony, line 206
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 207
		if (find_among_b(a_5) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_DA() {
		// (, line 210
		// call check_vowel_harmony, line 211
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 212
		if (find_among_b(a_6) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ndA() {
		// (, line 215
		// call check_vowel_harmony, line 216
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 217
		if (find_among_b(a_7) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_DAn() {
		// (, line 220
		// call check_vowel_harmony, line 221
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 222
		if (find_among_b(a_8) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ndAn() {
		// (, line 225
		// call check_vowel_harmony, line 226
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 227
		if (find_among_b(a_9) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ylA() {
		// (, line 230
		// call check_vowel_harmony, line 231
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 232
		if (find_among_b(a_10) == 0) {
			return false;
		}
		// (, line 233
		// call mark_suffix_with_optional_y_consonant, line 233
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ki() {
		// (, line 236
		// literal, line 237
		if (!(eq_s_b("ki"))) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ncA() {
		// (, line 240
		// call check_vowel_harmony, line 241
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 242
		if (find_among_b(a_11) == 0) {
			return false;
		}
		// (, line 243
		// call mark_suffix_with_optional_n_consonant, line 243
		if (!r_mark_suffix_with_optional_n_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_yUm() {
		// (, line 246
		// call check_vowel_harmony, line 247
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 248
		if (find_among_b(a_12) == 0) {
			return false;
		}
		// (, line 249
		// call mark_suffix_with_optional_y_consonant, line 249
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_sUn() {
		// (, line 252
		// call check_vowel_harmony, line 253
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 254
		if (find_among_b(a_13) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_yUz() {
		// (, line 257
		// call check_vowel_harmony, line 258
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 259
		if (find_among_b(a_14) == 0) {
			return false;
		}
		// (, line 260
		// call mark_suffix_with_optional_y_consonant, line 260
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_sUnUz() {
		// (, line 263
		// among, line 264
		if (find_among_b(a_15) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_lAr() {
		// (, line 267
		// call check_vowel_harmony, line 268
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 269
		if (find_among_b(a_16) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_nUz() {
		// (, line 272
		// call check_vowel_harmony, line 273
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 274
		if (find_among_b(a_17) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_DUr() {
		// (, line 277
		// call check_vowel_harmony, line 278
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 279
		if (find_among_b(a_18) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_cAsInA() {
		// (, line 282
		// among, line 283
		if (find_among_b(a_19) == 0) {
			return false;
		}
		return true;
	}

	private boolean r_mark_yDU() {
		// (, line 286
		// call check_vowel_harmony, line 287
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 288
		if (find_among_b(a_20) == 0) {
			return false;
		}
		// (, line 292
		// call mark_suffix_with_optional_y_consonant, line 292
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ysA() {
		// (, line 296
		// among, line 297
		if (find_among_b(a_21) == 0) {
			return false;
		}
		// (, line 298
		// call mark_suffix_with_optional_y_consonant, line 298
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_ymUs_() {
		// (, line 301
		// call check_vowel_harmony, line 302
		if (!r_check_vowel_harmony()) {
			return false;
		}
		// among, line 303
		if (find_among_b(a_22) == 0) {
			return false;
		}
		// (, line 304
		// call mark_suffix_with_optional_y_consonant, line 304
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_mark_yken() {
		// (, line 307
		// literal, line 308
		if (!(eq_s_b("ken"))) {
			return false;
		}
		// (, line 308
		// call mark_suffix_with_optional_y_consonant, line 308
		if (!r_mark_suffix_with_optional_y_consonant()) {
			return false;
		}
		return true;
	}

	private boolean r_stem_nominal_verb_suffixes() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		// (, line 311
		// [, line 312
		ket = cursor;
		// set continue_stemming_noun_suffixes, line 313
		B_continue_stemming_noun_suffixes = true;
		// or, line 315
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 314
				// or, line 314
				lab2: do {
					v_2 = limit - cursor;
					lab3: do {
						// call mark_ymUs_, line 314
						if (!r_mark_ymUs_()) {
							break lab3;
						}
						break lab2;
					} while (false);
					cursor = limit - v_2;
					lab4: do {
						// call mark_yDU, line 314
						if (!r_mark_yDU()) {
							break lab4;
						}
						break lab2;
					} while (false);
					cursor = limit - v_2;
					lab5: do {
						// call mark_ysA, line 314
						if (!r_mark_ysA()) {
							break lab5;
						}
						break lab2;
					} while (false);
					cursor = limit - v_2;
					// call mark_yken, line 314
					if (!r_mark_yken()) {
						break lab1;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab6: do {
				// (, line 316
				// call mark_cAsInA, line 316
				if (!r_mark_cAsInA()) {
					break lab6;
				}
				// (, line 316
				// or, line 316
				lab7: do {
					v_3 = limit - cursor;
					lab8: do {
						// call mark_sUnUz, line 316
						if (!r_mark_sUnUz()) {
							break lab8;
						}
						break lab7;
					} while (false);
					cursor = limit - v_3;
					lab9: do {
						// call mark_lAr, line 316
						if (!r_mark_lAr()) {
							break lab9;
						}
						break lab7;
					} while (false);
					cursor = limit - v_3;
					lab10: do {
						// call mark_yUm, line 316
						if (!r_mark_yUm()) {
							break lab10;
						}
						break lab7;
					} while (false);
					cursor = limit - v_3;
					lab11: do {
						// call mark_sUn, line 316
						if (!r_mark_sUn()) {
							break lab11;
						}
						break lab7;
					} while (false);
					cursor = limit - v_3;
					lab12: do {
						// call mark_yUz, line 316
						if (!r_mark_yUz()) {
							break lab12;
						}
						break lab7;
					} while (false);
					cursor = limit - v_3;
				} while (false);
				// call mark_ymUs_, line 316
				if (!r_mark_ymUs_()) {
					break lab6;
				}
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab13: do {
				// (, line 318
				// call mark_lAr, line 319
				if (!r_mark_lAr()) {
					break lab13;
				}
				// ], line 319
				bra = cursor;
				// delete, line 319
				slice_del();
				// try, line 319
				v_4 = limit - cursor;
				lab14: do {
					// (, line 319
					// [, line 319
					ket = cursor;
					// (, line 319
					// or, line 319
					lab15: do {
						v_5 = limit - cursor;
						lab16: do {
							// call mark_DUr, line 319
							if (!r_mark_DUr()) {
								break lab16;
							}
							break lab15;
						} while (false);
						cursor = limit - v_5;
						lab17: do {
							// call mark_yDU, line 319
							if (!r_mark_yDU()) {
								break lab17;
							}
							break lab15;
						} while (false);
						cursor = limit - v_5;
						lab18: do {
							// call mark_ysA, line 319
							if (!r_mark_ysA()) {
								break lab18;
							}
							break lab15;
						} while (false);
						cursor = limit - v_5;
						// call mark_ymUs_, line 319
						if (!r_mark_ymUs_()) {
							cursor = limit - v_4;
							break lab14;
						}
					} while (false);
				} while (false);
				// unset continue_stemming_noun_suffixes, line 320
				B_continue_stemming_noun_suffixes = false;
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab19: do {
				// (, line 323
				// call mark_nUz, line 323
				if (!r_mark_nUz()) {
					break lab19;
				}
				// (, line 323
				// or, line 323
				lab20: do {
					v_6 = limit - cursor;
					lab21: do {
						// call mark_yDU, line 323
						if (!r_mark_yDU()) {
							break lab21;
						}
						break lab20;
					} while (false);
					cursor = limit - v_6;
					// call mark_ysA, line 323
					if (!r_mark_ysA()) {
						break lab19;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab22: do {
				// (, line 325
				// (, line 325
				// or, line 325
				lab23: do {
					v_7 = limit - cursor;
					lab24: do {
						// call mark_sUnUz, line 325
						if (!r_mark_sUnUz()) {
							break lab24;
						}
						break lab23;
					} while (false);
					cursor = limit - v_7;
					lab25: do {
						// call mark_yUz, line 325
						if (!r_mark_yUz()) {
							break lab25;
						}
						break lab23;
					} while (false);
					cursor = limit - v_7;
					lab26: do {
						// call mark_sUn, line 325
						if (!r_mark_sUn()) {
							break lab26;
						}
						break lab23;
					} while (false);
					cursor = limit - v_7;
					// call mark_yUm, line 325
					if (!r_mark_yUm()) {
						break lab22;
					}
				} while (false);
				// ], line 325
				bra = cursor;
				// delete, line 325
				slice_del();
				// try, line 325
				v_8 = limit - cursor;
				lab27: do {
					// (, line 325
					// [, line 325
					ket = cursor;
					// call mark_ymUs_, line 325
					if (!r_mark_ymUs_()) {
						cursor = limit - v_8;
						break lab27;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 327
			// call mark_DUr, line 327
			if (!r_mark_DUr()) {
				return false;
			}
			// ], line 327
			bra = cursor;
			// delete, line 327
			slice_del();
			// try, line 327
			v_9 = limit - cursor;
			lab28: do {
				// (, line 327
				// [, line 327
				ket = cursor;
				// (, line 327
				// or, line 327
				lab29: do {
					v_10 = limit - cursor;
					lab30: do {
						// call mark_sUnUz, line 327
						if (!r_mark_sUnUz()) {
							break lab30;
						}
						break lab29;
					} while (false);
					cursor = limit - v_10;
					lab31: do {
						// call mark_lAr, line 327
						if (!r_mark_lAr()) {
							break lab31;
						}
						break lab29;
					} while (false);
					cursor = limit - v_10;
					lab32: do {
						// call mark_yUm, line 327
						if (!r_mark_yUm()) {
							break lab32;
						}
						break lab29;
					} while (false);
					cursor = limit - v_10;
					lab33: do {
						// call mark_sUn, line 327
						if (!r_mark_sUn()) {
							break lab33;
						}
						break lab29;
					} while (false);
					cursor = limit - v_10;
					lab34: do {
						// call mark_yUz, line 327
						if (!r_mark_yUz()) {
							break lab34;
						}
						break lab29;
					} while (false);
					cursor = limit - v_10;
				} while (false);
				// call mark_ymUs_, line 327
				if (!r_mark_ymUs_()) {
					cursor = limit - v_9;
					break lab28;
				}
			} while (false);
		} while (false);
		// ], line 328
		bra = cursor;
		// delete, line 328
		slice_del();
		return true;
	}

	private boolean r_stem_suffix_chain_before_ki() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		// (, line 332
		// [, line 333
		ket = cursor;
		// call mark_ki, line 334
		if (!r_mark_ki()) {
			return false;
		}
		// (, line 335
		// or, line 342
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 336
				// call mark_DA, line 336
				if (!r_mark_DA()) {
					break lab1;
				}
				// ], line 336
				bra = cursor;
				// delete, line 336
				slice_del();
				// try, line 336
				v_2 = limit - cursor;
				lab2: do {
					// (, line 336
					// [, line 336
					ket = cursor;
					// or, line 338
					lab3: do {
						v_3 = limit - cursor;
						lab4: do {
							// (, line 337
							// call mark_lAr, line 337
							if (!r_mark_lAr()) {
								break lab4;
							}
							// ], line 337
							bra = cursor;
							// delete, line 337
							slice_del();
							// try, line 337
							v_4 = limit - cursor;
							lab5: do {
								// (, line 337
								// call stem_suffix_chain_before_ki, line 337
								if (!r_stem_suffix_chain_before_ki()) {
									cursor = limit - v_4;
									break lab5;
								}
							} while (false);
							break lab3;
						} while (false);
						cursor = limit - v_3;
						// (, line 339
						// call mark_possessives, line 339
						if (!r_mark_possessives()) {
							cursor = limit - v_2;
							break lab2;
						}
						// ], line 339
						bra = cursor;
						// delete, line 339
						slice_del();
						// try, line 339
						v_5 = limit - cursor;
						lab6: do {
							// (, line 339
							// [, line 339
							ket = cursor;
							// call mark_lAr, line 339
							if (!r_mark_lAr()) {
								cursor = limit - v_5;
								break lab6;
							}
							// ], line 339
							bra = cursor;
							// delete, line 339
							slice_del();
							// call stem_suffix_chain_before_ki, line 339
							if (!r_stem_suffix_chain_before_ki()) {
								cursor = limit - v_5;
								break lab6;
							}
						} while (false);
					} while (false);
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab7: do {
				// (, line 343
				// call mark_nUn, line 343
				if (!r_mark_nUn()) {
					break lab7;
				}
				// ], line 343
				bra = cursor;
				// delete, line 343
				slice_del();
				// try, line 343
				v_6 = limit - cursor;
				lab8: do {
					// (, line 343
					// [, line 343
					ket = cursor;
					// or, line 345
					lab9: do {
						v_7 = limit - cursor;
						lab10: do {
							// (, line 344
							// call mark_lArI, line 344
							if (!r_mark_lArI()) {
								break lab10;
							}
							// ], line 344
							bra = cursor;
							// delete, line 344
							slice_del();
							break lab9;
						} while (false);
						cursor = limit - v_7;
						lab11: do {
							// (, line 346
							// [, line 346
							ket = cursor;
							// or, line 346
							lab12: do {
								v_8 = limit - cursor;
								lab13: do {
									// call mark_possessives, line 346
									if (!r_mark_possessives()) {
										break lab13;
									}
									break lab12;
								} while (false);
								cursor = limit - v_8;
								// call mark_sU, line 346
								if (!r_mark_sU()) {
									break lab11;
								}
							} while (false);
							// ], line 346
							bra = cursor;
							// delete, line 346
							slice_del();
							// try, line 346
							v_9 = limit - cursor;
							lab14: do {
								// (, line 346
								// [, line 346
								ket = cursor;
								// call mark_lAr, line 346
								if (!r_mark_lAr()) {
									cursor = limit - v_9;
									break lab14;
								}
								// ], line 346
								bra = cursor;
								// delete, line 346
								slice_del();
								// call stem_suffix_chain_before_ki, line 346
								if (!r_stem_suffix_chain_before_ki()) {
									cursor = limit - v_9;
									break lab14;
								}
							} while (false);
							break lab9;
						} while (false);
						cursor = limit - v_7;
						// (, line 348
						// call stem_suffix_chain_before_ki, line 348
						if (!r_stem_suffix_chain_before_ki()) {
							cursor = limit - v_6;
							break lab8;
						}
					} while (false);
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 351
			// call mark_ndA, line 351
			if (!r_mark_ndA()) {
				return false;
			}
			// (, line 351
			// or, line 353
			lab15: do {
				v_10 = limit - cursor;
				lab16: do {
					// (, line 352
					// call mark_lArI, line 352
					if (!r_mark_lArI()) {
						break lab16;
					}
					// ], line 352
					bra = cursor;
					// delete, line 352
					slice_del();
					break lab15;
				} while (false);
				cursor = limit - v_10;
				lab17: do {
					// (, line 354
					// (, line 354
					// call mark_sU, line 354
					if (!r_mark_sU()) {
						break lab17;
					}
					// ], line 354
					bra = cursor;
					// delete, line 354
					slice_del();
					// try, line 354
					v_11 = limit - cursor;
					lab18: do {
						// (, line 354
						// [, line 354
						ket = cursor;
						// call mark_lAr, line 354
						if (!r_mark_lAr()) {
							cursor = limit - v_11;
							break lab18;
						}
						// ], line 354
						bra = cursor;
						// delete, line 354
						slice_del();
						// call stem_suffix_chain_before_ki, line 354
						if (!r_stem_suffix_chain_before_ki()) {
							cursor = limit - v_11;
							break lab18;
						}
					} while (false);
					break lab15;
				} while (false);
				cursor = limit - v_10;
				// (, line 356
				// call stem_suffix_chain_before_ki, line 356
				if (!r_stem_suffix_chain_before_ki()) {
					return false;
				}
			} while (false);
		} while (false);
		return true;
	}

	private boolean r_stem_noun_suffixes() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		int v_14;
		int v_15;
		int v_16;
		int v_17;
		int v_18;
		int v_19;
		int v_20;
		int v_21;
		int v_22;
		int v_23;
		int v_24;
		int v_25;
		int v_26;
		int v_27;
		// (, line 361
		// or, line 363
		lab0: do {
			v_1 = limit - cursor;
			lab1: do {
				// (, line 362
				// [, line 362
				ket = cursor;
				// call mark_lAr, line 362
				if (!r_mark_lAr()) {
					break lab1;
				}
				// ], line 362
				bra = cursor;
				// delete, line 362
				slice_del();
				// try, line 362
				v_2 = limit - cursor;
				lab2: do {
					// (, line 362
					// call stem_suffix_chain_before_ki, line 362
					if (!r_stem_suffix_chain_before_ki()) {
						cursor = limit - v_2;
						break lab2;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab3: do {
				// (, line 364
				// [, line 364
				ket = cursor;
				// call mark_ncA, line 364
				if (!r_mark_ncA()) {
					break lab3;
				}
				// ], line 364
				bra = cursor;
				// delete, line 364
				slice_del();
				// try, line 365
				v_3 = limit - cursor;
				lab4: do {
					// (, line 365
					// or, line 367
					lab5: do {
						v_4 = limit - cursor;
						lab6: do {
							// (, line 366
							// [, line 366
							ket = cursor;
							// call mark_lArI, line 366
							if (!r_mark_lArI()) {
								break lab6;
							}
							// ], line 366
							bra = cursor;
							// delete, line 366
							slice_del();
							break lab5;
						} while (false);
						cursor = limit - v_4;
						lab7: do {
							// (, line 368
							// [, line 368
							ket = cursor;
							// or, line 368
							lab8: do {
								v_5 = limit - cursor;
								lab9: do {
									// call mark_possessives, line 368
									if (!r_mark_possessives()) {
										break lab9;
									}
									break lab8;
								} while (false);
								cursor = limit - v_5;
								// call mark_sU, line 368
								if (!r_mark_sU()) {
									break lab7;
								}
							} while (false);
							// ], line 368
							bra = cursor;
							// delete, line 368
							slice_del();
							// try, line 368
							v_6 = limit - cursor;
							lab10: do {
								// (, line 368
								// [, line 368
								ket = cursor;
								// call mark_lAr, line 368
								if (!r_mark_lAr()) {
									cursor = limit - v_6;
									break lab10;
								}
								// ], line 368
								bra = cursor;
								// delete, line 368
								slice_del();
								// call stem_suffix_chain_before_ki, line 368
								if (!r_stem_suffix_chain_before_ki()) {
									cursor = limit - v_6;
									break lab10;
								}
							} while (false);
							break lab5;
						} while (false);
						cursor = limit - v_4;
						// (, line 370
						// [, line 370
						ket = cursor;
						// call mark_lAr, line 370
						if (!r_mark_lAr()) {
							cursor = limit - v_3;
							break lab4;
						}
						// ], line 370
						bra = cursor;
						// delete, line 370
						slice_del();
						// call stem_suffix_chain_before_ki, line 370
						if (!r_stem_suffix_chain_before_ki()) {
							cursor = limit - v_3;
							break lab4;
						}
					} while (false);
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab11: do {
				// (, line 374
				// [, line 374
				ket = cursor;
				// (, line 374
				// or, line 374
				lab12: do {
					v_7 = limit - cursor;
					lab13: do {
						// call mark_ndA, line 374
						if (!r_mark_ndA()) {
							break lab13;
						}
						break lab12;
					} while (false);
					cursor = limit - v_7;
					// call mark_nA, line 374
					if (!r_mark_nA()) {
						break lab11;
					}
				} while (false);
				// (, line 375
				// or, line 377
				lab14: do {
					v_8 = limit - cursor;
					lab15: do {
						// (, line 376
						// call mark_lArI, line 376
						if (!r_mark_lArI()) {
							break lab15;
						}
						// ], line 376
						bra = cursor;
						// delete, line 376
						slice_del();
						break lab14;
					} while (false);
					cursor = limit - v_8;
					lab16: do {
						// (, line 378
						// call mark_sU, line 378
						if (!r_mark_sU()) {
							break lab16;
						}
						// ], line 378
						bra = cursor;
						// delete, line 378
						slice_del();
						// try, line 378
						v_9 = limit - cursor;
						lab17: do {
							// (, line 378
							// [, line 378
							ket = cursor;
							// call mark_lAr, line 378
							if (!r_mark_lAr()) {
								cursor = limit - v_9;
								break lab17;
							}
							// ], line 378
							bra = cursor;
							// delete, line 378
							slice_del();
							// call stem_suffix_chain_before_ki, line 378
							if (!r_stem_suffix_chain_before_ki()) {
								cursor = limit - v_9;
								break lab17;
							}
						} while (false);
						break lab14;
					} while (false);
					cursor = limit - v_8;
					// (, line 380
					// call stem_suffix_chain_before_ki, line 380
					if (!r_stem_suffix_chain_before_ki()) {
						break lab11;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab18: do {
				// (, line 384
				// [, line 384
				ket = cursor;
				// (, line 384
				// or, line 384
				lab19: do {
					v_10 = limit - cursor;
					lab20: do {
						// call mark_ndAn, line 384
						if (!r_mark_ndAn()) {
							break lab20;
						}
						break lab19;
					} while (false);
					cursor = limit - v_10;
					// call mark_nU, line 384
					if (!r_mark_nU()) {
						break lab18;
					}
				} while (false);
				// (, line 384
				// or, line 384
				lab21: do {
					v_11 = limit - cursor;
					lab22: do {
						// (, line 384
						// call mark_sU, line 384
						if (!r_mark_sU()) {
							break lab22;
						}
						// ], line 384
						bra = cursor;
						// delete, line 384
						slice_del();
						// try, line 384
						v_12 = limit - cursor;
						lab23: do {
							// (, line 384
							// [, line 384
							ket = cursor;
							// call mark_lAr, line 384
							if (!r_mark_lAr()) {
								cursor = limit - v_12;
								break lab23;
							}
							// ], line 384
							bra = cursor;
							// delete, line 384
							slice_del();
							// call stem_suffix_chain_before_ki, line 384
							if (!r_stem_suffix_chain_before_ki()) {
								cursor = limit - v_12;
								break lab23;
							}
						} while (false);
						break lab21;
					} while (false);
					cursor = limit - v_11;
					// (, line 384
					// call mark_lArI, line 384
					if (!r_mark_lArI()) {
						break lab18;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab24: do {
				// (, line 386
				// [, line 386
				ket = cursor;
				// call mark_DAn, line 386
				if (!r_mark_DAn()) {
					break lab24;
				}
				// ], line 386
				bra = cursor;
				// delete, line 386
				slice_del();
				// try, line 386
				v_13 = limit - cursor;
				lab25: do {
					// (, line 386
					// [, line 386
					ket = cursor;
					// (, line 387
					// or, line 389
					lab26: do {
						v_14 = limit - cursor;
						lab27: do {
							// (, line 388
							// call mark_possessives, line 388
							if (!r_mark_possessives()) {
								break lab27;
							}
							// ], line 388
							bra = cursor;
							// delete, line 388
							slice_del();
							// try, line 388
							v_15 = limit - cursor;
							lab28: do {
								// (, line 388
								// [, line 388
								ket = cursor;
								// call mark_lAr, line 388
								if (!r_mark_lAr()) {
									cursor = limit - v_15;
									break lab28;
								}
								// ], line 388
								bra = cursor;
								// delete, line 388
								slice_del();
								// call stem_suffix_chain_before_ki, line 388
								if (!r_stem_suffix_chain_before_ki()) {
									cursor = limit - v_15;
									break lab28;
								}
							} while (false);
							break lab26;
						} while (false);
						cursor = limit - v_14;
						lab29: do {
							// (, line 390
							// call mark_lAr, line 390
							if (!r_mark_lAr()) {
								break lab29;
							}
							// ], line 390
							bra = cursor;
							// delete, line 390
							slice_del();
							// try, line 390
							v_16 = limit - cursor;
							lab30: do {
								// (, line 390
								// call stem_suffix_chain_before_ki, line 390
								if (!r_stem_suffix_chain_before_ki()) {
									cursor = limit - v_16;
									break lab30;
								}
							} while (false);
							break lab26;
						} while (false);
						cursor = limit - v_14;
						// (, line 392
						// call stem_suffix_chain_before_ki, line 392
						if (!r_stem_suffix_chain_before_ki()) {
							cursor = limit - v_13;
							break lab25;
						}
					} while (false);
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab31: do {
				// (, line 396
				// [, line 396
				ket = cursor;
				// or, line 396
				lab32: do {
					v_17 = limit - cursor;
					lab33: do {
						// call mark_nUn, line 396
						if (!r_mark_nUn()) {
							break lab33;
						}
						break lab32;
					} while (false);
					cursor = limit - v_17;
					// call mark_ylA, line 396
					if (!r_mark_ylA()) {
						break lab31;
					}
				} while (false);
				// ], line 396
				bra = cursor;
				// delete, line 396
				slice_del();
				// try, line 397
				v_18 = limit - cursor;
				lab34: do {
					// (, line 397
					// or, line 399
					lab35: do {
						v_19 = limit - cursor;
						lab36: do {
							// (, line 398
							// [, line 398
							ket = cursor;
							// call mark_lAr, line 398
							if (!r_mark_lAr()) {
								break lab36;
							}
							// ], line 398
							bra = cursor;
							// delete, line 398
							slice_del();
							// call stem_suffix_chain_before_ki, line 398
							if (!r_stem_suffix_chain_before_ki()) {
								break lab36;
							}
							break lab35;
						} while (false);
						cursor = limit - v_19;
						lab37: do {
							// (, line 400
							// [, line 400
							ket = cursor;
							// or, line 400
							lab38: do {
								v_20 = limit - cursor;
								lab39: do {
									// call mark_possessives, line 400
									if (!r_mark_possessives()) {
										break lab39;
									}
									break lab38;
								} while (false);
								cursor = limit - v_20;
								// call mark_sU, line 400
								if (!r_mark_sU()) {
									break lab37;
								}
							} while (false);
							// ], line 400
							bra = cursor;
							// delete, line 400
							slice_del();
							// try, line 400
							v_21 = limit - cursor;
							lab40: do {
								// (, line 400
								// [, line 400
								ket = cursor;
								// call mark_lAr, line 400
								if (!r_mark_lAr()) {
									cursor = limit - v_21;
									break lab40;
								}
								// ], line 400
								bra = cursor;
								// delete, line 400
								slice_del();
								// call stem_suffix_chain_before_ki, line 400
								if (!r_stem_suffix_chain_before_ki()) {
									cursor = limit - v_21;
									break lab40;
								}
							} while (false);
							break lab35;
						} while (false);
						cursor = limit - v_19;
						// call stem_suffix_chain_before_ki, line 402
						if (!r_stem_suffix_chain_before_ki()) {
							cursor = limit - v_18;
							break lab34;
						}
					} while (false);
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab41: do {
				// (, line 406
				// [, line 406
				ket = cursor;
				// call mark_lArI, line 406
				if (!r_mark_lArI()) {
					break lab41;
				}
				// ], line 406
				bra = cursor;
				// delete, line 406
				slice_del();
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab42: do {
				// (, line 408
				// call stem_suffix_chain_before_ki, line 408
				if (!r_stem_suffix_chain_before_ki()) {
					break lab42;
				}
				break lab0;
			} while (false);
			cursor = limit - v_1;
			lab43: do {
				// (, line 410
				// [, line 410
				ket = cursor;
				// or, line 410
				lab44: do {
					v_22 = limit - cursor;
					lab45: do {
						// call mark_DA, line 410
						if (!r_mark_DA()) {
							break lab45;
						}
						break lab44;
					} while (false);
					cursor = limit - v_22;
					lab46: do {
						// call mark_yU, line 410
						if (!r_mark_yU()) {
							break lab46;
						}
						break lab44;
					} while (false);
					cursor = limit - v_22;
					// call mark_yA, line 410
					if (!r_mark_yA()) {
						break lab43;
					}
				} while (false);
				// ], line 410
				bra = cursor;
				// delete, line 410
				slice_del();
				// try, line 410
				v_23 = limit - cursor;
				lab47: do {
					// (, line 410
					// [, line 410
					ket = cursor;
					// (, line 410
					// or, line 410
					lab48: do {
						v_24 = limit - cursor;
						lab49: do {
							// (, line 410
							// call mark_possessives, line 410
							if (!r_mark_possessives()) {
								break lab49;
							}
							// ], line 410
							bra = cursor;
							// delete, line 410
							slice_del();
							// try, line 410
							v_25 = limit - cursor;
							lab50: do {
								// (, line 410
								// [, line 410
								ket = cursor;
								// call mark_lAr, line 410
								if (!r_mark_lAr()) {
									cursor = limit - v_25;
									break lab50;
								}
							} while (false);
							break lab48;
						} while (false);
						cursor = limit - v_24;
						// call mark_lAr, line 410
						if (!r_mark_lAr()) {
							cursor = limit - v_23;
							break lab47;
						}
					} while (false);
					// ], line 410
					bra = cursor;
					// delete, line 410
					slice_del();
					// [, line 410
					ket = cursor;
					// call stem_suffix_chain_before_ki, line 410
					if (!r_stem_suffix_chain_before_ki()) {
						cursor = limit - v_23;
						break lab47;
					}
				} while (false);
				break lab0;
			} while (false);
			cursor = limit - v_1;
			// (, line 412
			// [, line 412
			ket = cursor;
			// or, line 412
			lab51: do {
				v_26 = limit - cursor;
				lab52: do {
					// call mark_possessives, line 412
					if (!r_mark_possessives()) {
						break lab52;
					}
					break lab51;
				} while (false);
				cursor = limit - v_26;
				// call mark_sU, line 412
				if (!r_mark_sU()) {
					return false;
				}
			} while (false);
			// ], line 412
			bra = cursor;
			// delete, line 412
			slice_del();
			// try, line 412
			v_27 = limit - cursor;
			lab53: do {
				// (, line 412
				// [, line 412
				ket = cursor;
				// call mark_lAr, line 412
				if (!r_mark_lAr()) {
					cursor = limit - v_27;
					break lab53;
				}
				// ], line 412
				bra = cursor;
				// delete, line 412
				slice_del();
				// call stem_suffix_chain_before_ki, line 412
				if (!r_stem_suffix_chain_before_ki()) {
					cursor = limit - v_27;
					break lab53;
				}
			} while (false);
		} while (false);
		return true;
	}

	private boolean r_post_process_last_consonants() {
		int among_var;
		// (, line 415
		// [, line 416
		ket = cursor;
		// substring, line 416
		among_var = find_among_b(a_23);
		if (among_var == 0) {
			return false;
		}
		// ], line 416
		bra = cursor;
		switch (among_var) {
		case 0:
			return false;
		case 1:
			// (, line 417
			// <-, line 417
			slice_from("p");
			break;
		case 2:
			// (, line 418
			// <-, line 418
			slice_from("\u00E7");
			break;
		case 3:
			// (, line 419
			// <-, line 419
			slice_from("t");
			break;
		case 4:
			// (, line 420
			// <-, line 420
			slice_from("k");
			break;
		}
		return true;
	}

	private boolean r_append_U_to_stems_ending_with_d_or_g() {
		int v_1;
		int v_2;
		int v_3;
		int v_4;
		int v_5;
		int v_6;
		int v_7;
		int v_8;
		int v_9;
		int v_10;
		int v_11;
		int v_12;
		int v_13;
		int v_14;
		int v_15;
		// (, line 430
		// test, line 431
		v_1 = limit - cursor;
		// (, line 431
		// or, line 431
		lab0: do {
			v_2 = limit - cursor;
			lab1: do {
				// literal, line 431
				if (!(eq_s_b("d"))) {
					break lab1;
				}
				break lab0;
			} while (false);
			cursor = limit - v_2;
			// literal, line 431
			if (!(eq_s_b("g"))) {
				return false;
			}
		} while (false);
		cursor = limit - v_1;
		// or, line 433
		lab2: do {
			v_3 = limit - cursor;
			lab3: do {
				// (, line 432
				// test, line 432
				v_4 = limit - cursor;
				// (, line 432
				// (, line 432
				// goto, line 432
				golab4: while (true) {
					v_5 = limit - cursor;
					lab5: do {
						if (!(in_grouping_b(g_vowel, 97, 305))) {
							break lab5;
						}
						cursor = limit - v_5;
						break golab4;
					} while (false);
					cursor = limit - v_5;
					if (cursor <= limit_backward) {
						break lab3;
					}
					cursor--;
				}
				// or, line 432
				lab6: do {
					v_6 = limit - cursor;
					lab7: do {
						// literal, line 432
						if (!(eq_s_b("a"))) {
							break lab7;
						}
						break lab6;
					} while (false);
					cursor = limit - v_6;
					// literal, line 432
					if (!(eq_s_b("\u0131"))) {
						break lab3;
					}
				} while (false);
				cursor = limit - v_4;
				// <+, line 432
				{
					int c = cursor;
					insert(cursor, cursor, "\u0131");
					cursor = c;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab8: do {
				// (, line 434
				// test, line 434
				v_7 = limit - cursor;
				// (, line 434
				// (, line 434
				// goto, line 434
				golab9: while (true) {
					v_8 = limit - cursor;
					lab10: do {
						if (!(in_grouping_b(g_vowel, 97, 305))) {
							break lab10;
						}
						cursor = limit - v_8;
						break golab9;
					} while (false);
					cursor = limit - v_8;
					if (cursor <= limit_backward) {
						break lab8;
					}
					cursor--;
				}
				// or, line 434
				lab11: do {
					v_9 = limit - cursor;
					lab12: do {
						// literal, line 434
						if (!(eq_s_b("e"))) {
							break lab12;
						}
						break lab11;
					} while (false);
					cursor = limit - v_9;
					// literal, line 434
					if (!(eq_s_b("i"))) {
						break lab8;
					}
				} while (false);
				cursor = limit - v_7;
				// <+, line 434
				{
					int c = cursor;
					insert(cursor, cursor, "i");
					cursor = c;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			lab13: do {
				// (, line 436
				// test, line 436
				v_10 = limit - cursor;
				// (, line 436
				// (, line 436
				// goto, line 436
				golab14: while (true) {
					v_11 = limit - cursor;
					lab15: do {
						if (!(in_grouping_b(g_vowel, 97, 305))) {
							break lab15;
						}
						cursor = limit - v_11;
						break golab14;
					} while (false);
					cursor = limit - v_11;
					if (cursor <= limit_backward) {
						break lab13;
					}
					cursor--;
				}
				// or, line 436
				lab16: do {
					v_12 = limit - cursor;
					lab17: do {
						// literal, line 436
						if (!(eq_s_b("o"))) {
							break lab17;
						}
						break lab16;
					} while (false);
					cursor = limit - v_12;
					// literal, line 436
					if (!(eq_s_b("u"))) {
						break lab13;
					}
				} while (false);
				cursor = limit - v_10;
				// <+, line 436
				{
					int c = cursor;
					insert(cursor, cursor, "u");
					cursor = c;
				}
				break lab2;
			} while (false);
			cursor = limit - v_3;
			// (, line 438
			// test, line 438
			v_13 = limit - cursor;
			// (, line 438
			// (, line 438
			// goto, line 438
			golab18: while (true) {
				v_14 = limit - cursor;
				lab19: do {
					if (!(in_grouping_b(g_vowel, 97, 305))) {
						break lab19;
					}
					cursor = limit - v_14;
					break golab18;
				} while (false);
				cursor = limit - v_14;
				if (cursor <= limit_backward) {
					return false;
				}
				cursor--;
			}
			// or, line 438
			lab20: do {
				v_15 = limit - cursor;
				lab21: do {
					// literal, line 438
					if (!(eq_s_b("\u00F6"))) {
						break lab21;
					}
					break lab20;
				} while (false);
				cursor = limit - v_15;
				// literal, line 438
				if (!(eq_s_b("\u00FC"))) {
					return false;
				}
			} while (false);
			cursor = limit - v_13;
			// <+, line 438
			{
				int c = cursor;
				insert(cursor, cursor, "\u00FC");
				cursor = c;
			}
		} while (false);
		return true;
	}

	private boolean r_more_than_one_syllable_word() {
		int v_1;
		int v_3;
		// (, line 445
		// test, line 446
		v_1 = cursor;
		// (, line 446
		// atleast, line 446
		{
			int v_2 = 2;
			// atleast, line 446
			replab0: while (true) {
				v_3 = cursor;
				lab1: do {
					// (, line 446
					// gopast, line 446
					golab2: while (true) {
						lab3: do {
							if (!(in_grouping(g_vowel, 97, 305))) {
								break lab3;
							}
							break golab2;
						} while (false);
						if (cursor >= limit) {
							break lab1;
						}
						cursor++;
					}
					v_2--;
					continue replab0;
				} while (false);
				cursor = v_3;
				break replab0;
			}
			if (v_2 > 0) {
				return false;
			}
		}
		cursor = v_1;
		return true;
	}

	private boolean r_is_reserved_word() {
		int v_1;
		int v_2;
		int v_4;
		// (, line 449
		// or, line 451
		lab0: do {
			v_1 = cursor;
			lab1: do {
				// test, line 450
				v_2 = cursor;
				// (, line 450
				// gopast, line 450
				golab2: while (true) {
					lab3: do {
						// literal, line 450
						if (!(eq_s("ad"))) {
							break lab3;
						}
						break golab2;
					} while (false);
					if (cursor >= limit) {
						break lab1;
					}
					cursor++;
				}
				// (, line 450
				I_strlen = 2;
				// (, line 450
				if (!(I_strlen == limit)) {
					break lab1;
				}
				cursor = v_2;
				break lab0;
			} while (false);
			cursor = v_1;
			// test, line 452
			v_4 = cursor;
			// (, line 452
			// gopast, line 452
			golab4: while (true) {
				lab5: do {
					// literal, line 452
					if (!(eq_s("soyad"))) {
						break lab5;
					}
					break golab4;
				} while (false);
				if (cursor >= limit) {
					return false;
				}
				cursor++;
			}
			// (, line 452
			I_strlen = 5;
			// (, line 452
			if (!(I_strlen == limit)) {
				return false;
			}
			cursor = v_4;
		} while (false);
		return true;
	}

	private boolean r_postlude() {
		int v_1;
		int v_2;
		int v_3;
		// (, line 455
		// not, line 456
		{
			v_1 = cursor;
			lab0: do {
				// (, line 456
				// call is_reserved_word, line 456
				if (!r_is_reserved_word()) {
					break lab0;
				}
				return false;
			} while (false);
			cursor = v_1;
		}
		// backwards, line 457
		limit_backward = cursor;
		cursor = limit;
		// (, line 457
		// do, line 458
		v_2 = limit - cursor;
		lab1: do {
			// call append_U_to_stems_ending_with_d_or_g, line 458
			if (!r_append_U_to_stems_ending_with_d_or_g()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		// do, line 459
		v_3 = limit - cursor;
		lab2: do {
			// call post_process_last_consonants, line 459
			if (!r_post_process_last_consonants()) {
				break lab2;
			}
		} while (false);
		cursor = limit - v_3;
		cursor = limit_backward;
		return true;
	}

	@Override
	public boolean stem() {
		int v_1;
		int v_2;
		// (, line 464
		// (, line 465
		// call more_than_one_syllable_word, line 465
		if (!r_more_than_one_syllable_word()) {
			return false;
		}
		// (, line 466
		// backwards, line 467
		limit_backward = cursor;
		cursor = limit;
		// (, line 467
		// do, line 468
		v_1 = limit - cursor;
		lab0: do {
			// call stem_nominal_verb_suffixes, line 468
			if (!r_stem_nominal_verb_suffixes()) {
				break lab0;
			}
		} while (false);
		cursor = limit - v_1;
		// Boolean test continue_stemming_noun_suffixes, line 469
		if (!(B_continue_stemming_noun_suffixes)) {
			return false;
		}
		// do, line 470
		v_2 = limit - cursor;
		lab1: do {
			// call stem_noun_suffixes, line 470
			if (!r_stem_noun_suffixes()) {
				break lab1;
			}
		} while (false);
		cursor = limit - v_2;
		cursor = limit_backward; // call postlude, line 473
		if (!r_postlude()) {
			return false;
		}
		return true;
	}

}
