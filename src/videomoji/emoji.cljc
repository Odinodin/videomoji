(ns videomoji.emoji)

(def monochrome-palette
  [{:emoji "&#x1F5A4;" :rgb [15 15 15]},
   {:emoji "&#x1F977;" :rgb [35 35 35]},
   {:emoji "&#x1F98D;" :rgb [55 55 55]},
   {:emoji "&#x1F9BE;" :rgb [75 75 75]},
   {:emoji "&#x1F993;" :rgb [95 95 95]},
   {:emoji "&#x1F463;" :rgb [115 115 115]},
   {:emoji "&#x1F47b;" :rgb [135 135 135]},
   {:emoji "&#x1F480;" :rgb [155 155 155]},
   {:emoji "&#x1F440;" :rgb [175 175 175]},
   {:emoji "&#x1F9B4;" :rgb [195 195 195]},
   {:emoji "&#x1F90D;" :rgb [215 215 215]},
   {:emoji "&#x1F4AC;" :rgb [235 235 235]},
   {:emoji "&#x1F5EF;" :rgb [255 255 255]}])

(def color-grayed-palette
  [;; 🖤 Blacks & dark grays
   {:emoji "⬛" :rgb [0 0 0]}
   {:emoji "🖤" :rgb [30 30 30]}
   {:emoji "⚫" :rgb [50 50 50]}
   {:emoji "🎩" :rgb [60 60 60]}
   {:emoji "🎱" :rgb [80 80 80]}
   {:emoji "🕶" :rgb [100 100 100]}
   {:emoji "💼" :rgb [110 110 110]}

   ;; 🩶 Mid grays
   {:emoji "🩶" :rgb [128 128 128]}
   {:emoji "🧳" :rgb [140 140 140]}
   {:emoji "🧥" :rgb [150 150 150]}
   {:emoji "📎" :rgb [160 160 160]}

   ;; 🤍 Light grays & whites
   {:emoji "🗂" :rgb [180 180 180]}
   {:emoji "📄" :rgb [220 220 220]}
   {:emoji "🧻" :rgb [240 240 240]}
   {:emoji "⚪" :rgb [250 250 250]}
   {:emoji "⬜" :rgb [255 255 255]}

   ;; 🔴 Reds
   {:emoji "🟥" :rgb [255 0 0]}
   {:emoji "🍎" :rgb [230 30 30]}
   {:emoji "🍒" :rgb [222 49 99]}

   ;; 🟠 Oranges
   {:emoji "🟧" :rgb [255 165 0]}
   {:emoji "🍊" :rgb [255 140 0]}
   {:emoji "🥕" :rgb [255 110 40]}

   ;; 🟡 Yellows
   {:emoji "🟨" :rgb [255 255 0]}
   {:emoji "🍌" :rgb [255 240 100]}

   ;; 🟢 Greens
   {:emoji "🟩" :rgb [0 128 0]}
   {:emoji "🥝" :rgb [140 200 70]}
   {:emoji "🥦" :rgb [90 150 80]}

   ;; 🔵 Blues
   {:emoji "🟦" :rgb [0 0 255]}
   {:emoji "🫐" :rgb [70 100 200]}
   {:emoji "🧊" :rgb [160 230 255]}

   ;; 🟣 Purples
   {:emoji "🟪" :rgb [128 0 128]}
   {:emoji "🍇" :rgb [150 60 160]}
   {:emoji "🔮" :rgb [120 0 200]}

   ;; 🟤 Browns
   {:emoji "🟫" :rgb [139 69 19]}
   {:emoji "🍫" :rgb [123 63 0]}
   {:emoji "🥔" :rgb [205 133 63]}
   {:emoji "🪵" :rgb [165 94 54]}])

(def colored-palette
  [;; Reds
   {:emoji "🟥" :rgb [255 0 0]}
   {:emoji "🍎" :rgb [220 20 60]}
   {:emoji "🍒" :rgb [222 49 99]}

   ;; Oranges
   {:emoji "🟧" :rgb [255 165 0]}
   {:emoji "🍊" :rgb [255 140 0]}
   {:emoji "🧡" :rgb [255 130 80]}

   ;; Yellows
   {:emoji "🟨" :rgb [255 255 0]}
   {:emoji "🍌" :rgb [255 240 0]}
   {:emoji "🌕" :rgb [255 250 180]}

   ;; Greens
   {:emoji "🟩" :rgb [0 128 0]}
   {:emoji "🥝" :rgb [110 190 50]}
   {:emoji "🥦" :rgb [85 130 70]}

   ;; Blues
   {:emoji "🟦" :rgb [0 0 255]}
   {:emoji "🫐" :rgb [60 90 200]}
   {:emoji "🧊" :rgb [150 230 255]}

   ;; Purples
   {:emoji "🟪" :rgb [128 0 128]}
   {:emoji "🍇" :rgb [140 60 180]}
   {:emoji "🔮" :rgb [120 0 255]}

   ;; Browns
   {:emoji "🟫" :rgb [139 69 19]}
   {:emoji "🍫" :rgb [123 63 0]}
   {:emoji "🥔" :rgb [205 133 63]}

   ;; Grayscale
   {:emoji "⬛" :rgb [0 0 0]}
   {:emoji "⚫" :rgb [50 50 50]}
   {:emoji "⚪" :rgb [230 230 230]}
   {:emoji "⬜" :rgb [255 255 255]}

   ;; Pinks
   {:emoji "🌸" :rgb [255 182 193]}
   {:emoji "🎀" :rgb [255 105 180]}

   ;; Neutrals / Misc
   {:emoji "🌈" :rgb [150 150 150]}                         ;; rainbow, used for ambiguous colors
   ])

(def square-palette
  [{:emoji "🟥" :rgb [255 0 0]}
   {:emoji "🟧" :rgb [255 165 0]}
   {:emoji "🟨" :rgb [255 255 0]}
   {:emoji "🟩" :rgb [0 128 0]}
   {:emoji "🟦" :rgb [0 0 255]}
   {:emoji "🟪" :rgb [128 0 128]}
   {:emoji "⬛" :rgb [0 0 0]}
   {:emoji "🟨" :rgb [254 254 254]}
   {:emoji "⬜" :rgb [255 255 255]}
   {:emoji "🟫" :rgb [155 15 15]}])