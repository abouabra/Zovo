import tailwindcssAnimate from "tailwindcss-animate";

const tailwindConfig = {
	darkMode: "class", // Enable dark mode support (using class strategy)
	content: [
		"./app/**/*.{js,ts,jsx,tsx}",
		"./pages/**/*.{js,ts,jsx,tsx}",
		"./components/**/*.{js,ts,jsx,tsx}",
		"./src/**/*.{js,ts,jsx,tsx}", // Add this if using src directory
	],
	theme: {
		extend: {
			fontFamily: {
				sans: ["var(--font-roboto)", "sans-serif"],
			},
			colors: {
        blue: {
          50:  "#EBF5FF",
          100: "#E0F0FF",
          200: "#CCDFFF",
          300: "#669FFF",
          400: "#337EFF",
          500: "#005FFF",
          600: "#004CCC",
          700: "#003999",
          800: "#002666",
          900: "#00163D",
          950: "#001333",
        },
        red: {
          50:  "#FFF5F5",
          100: "#FFE5E7",
          200: "#FF999F",
          300: "#FF666E",
          400: "#FF3742",
          500: "#FF000E",
          600: "#CC000B",
          700: "#990008",
          800: "#660006",
          900: "#330003",
          950: "#1F0002",
        },
        green: {
          50:  "#F6FEF9",
          100: "#E9F1FF",
          200: "#A6F2C6",
          300: "#79ECA9",
          400: "#4CE68C",
          500: "#20E070",
          600: "#19B359",
          700: "#138643",
          800: "#0D592C",
          900: "#062D16",
          950: "#041B0D",
        },
        yellow: {
          50:  "#FFFCF5",
          100: "#FFF8E5",
          200: "#FFF1CC",
          300: "#FFE299",
          400: "#FFD466",
          500: "#FFB700",
          600: "#CC9200",
          700: "#996E00",
          800: "#664900",
          900: "#332500",
          950: "#1D1A11",
        },
        grey: {
          50:  "#FFFFFF",
          100: "#F4F4F5",
          200: "#E9EAED",
          300: "#DBDDE1",
          400: "#B4B7BB",
          500: "#747881",
          600: "#4C525C",
          700: "#272A30",
          800: "#1C1E22",
          900: "#17191C",
          950: "#080707",
        },
				"text-high-emphasis": "var(--text-high-emphasis)",
			},
		},
	},
	plugins: [tailwindcssAnimate],
};

export default tailwindConfig;
