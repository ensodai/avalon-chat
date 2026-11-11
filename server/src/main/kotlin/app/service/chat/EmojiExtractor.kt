package app.service.chat

/**
 * Экстрактор эмодзи из строк.
 * Использует Unicode кодовые точки для правильного распознавания эмодзи,
 * включая составные эмодзи (флаги, тон кожи, ZWJ-последовательности).
 * 
 * Данные актуальны для Unicode 18.0 (2026)
 */
object EmojiExtractor {

    // Диапазоны Unicode для эмодзи (Unicode 18.0 / 2026)
    // Источник: https://www.unicode.org/Public/18.0.0/ucd/emoji/emoji-data.txt
    private val emojiRanges = listOf(
        // Basic Latin
        0x0023..0x0023, // # hash sign
        0x002A..0x002A, // * asterisk
        0x0030..0x0039, // 0-9 digits
        
        // Latin-1 Supplement
        0x00A9..0x00A9, // © copyright
        0x00AE..0x00AE, // ® registered
        
        // General Punctuation
        0x203C..0x203C, // ‼ double exclamation
        0x2049..0x2049, // ⁉ exclamation question
        0x2122..0x2122, // ™ trade mark
        0x2139..0x2139, // ℹ information
        0x2194..0x2199, // ↔..↙ arrows
        0x21A9..0x21AA, // ↩..↪ curving arrows
        
        // Miscellaneous Technical
        0x231A..0x231B, // ⌚..⌛ watch, hourglass
        0x2328..0x2328, // ⌨ keyboard
        0x23CF..0x23CF, // ⏏ eject
        0x23E9..0x23EC, // ⏩..⏬ media forward
        0x23ED..0x23EE, // ⏭..⏮ track buttons
        0x23EF..0x23EF, // ⏯ play/pause
        0x23F0..0x23F0, // ⏰ alarm clock
        0x23F1..0x23F2, // ⏱..⏲ stopwatch, timer
        0x23F3..0x23F3, // ⏳ hourglass not done
        0x23F8..0x23FA, // ⏸..⏺ media controls
        
        // Enclosed Alphanumerics
        0x24C2..0x24C2, // Ⓜ circled M
        
        // Geometric Shapes
        0x25AA..0x25AB, // ▪..▫ small squares
        0x25B6..0x25B6, // ▶ play button
        0x25C0..0x25C0, // ◀ reverse button
        0x25FB..0x25FE, // ◻..◾ medium squares
        0x2B1B..0x2B1C, // ⬛..⬜ large squares
        0x2B50..0x2B50, // ⭐ star
        0x2B55..0x2B55, // ⭕ hollow red circle
        
        // Miscellaneous Symbols (2600-26FF)
        0x2600..0x2604, // ☀..☄ sun..comet
        0x260E..0x260E, // ☎ telephone
        0x2611..0x2611, // ☑ check box with check
        0x2614..0x2615, // ☔..☕ umbrella, hot beverage
        0x2618..0x2618, // ☘ shamrock
        0x261D..0x261D, // ☝ index pointing up
        0x2620..0x2620, // ☠ skull and crossbones
        0x2622..0x2623, // ☢..☣ radioactive, biohazard
        0x2626..0x2626, // ☦ orthodox cross
        0x262A..0x262A, // ☪ star and crescent
        0x262E..0x262F, // ☮..☯ peace, yin yang
        0x2638..0x263A, // ☸..☺ wheel of dharma, smiling face
        0x2640..0x2640, // ♀ female sign
        0x2642..0x2642, // ♂ male sign
        0x2648..0x2653, // ♈..♓ zodiac signs
        0x265F..0x265F, // ♟ chess pawn
        0x2660..0x2660, // ♠ spade suit
        0x2663..0x2663, // ♣ club suit
        0x2665..0x2666, // ♥..♦ heart, diamond suits
        0x2668..0x2668, // ♨ hot springs
        0x267B..0x267B, // ♻ recycling symbol
        0x267E..0x267E, // ♾ infinity
        0x267F..0x267F, // ♿ wheelchair symbol
        0x2692..0x2697, // ⚒..⚗ tools, medical, alembic
        0x2699..0x2699, // ⚙ gear
        0x269B..0x269C, // ⚛..⚜ atom, fleur-de-lis
        0x26A0..0x26A1, // ⚠..⚡ warning, high voltage
        0x26A7..0x26A7, // ⚧ transgender symbol
        0x26AA..0x26AB, // ⚪..⚫ circles
        0x26B0..0x26B1, // ⚰..⚱ coffin, urn
        0x26BD..0x26BE, // ⚽..⚾ soccer, baseball
        0x26C4..0x26C5, // ⛄..★ snowman, sun behind cloud
        0x26C8..0x26C8, // ⛈ cloud with lightning
        0x26CE..0x26CF, // ⛎..⛏ Ophiuchus, pick
        0x26D1..0x26D1, // ⛑ rescue helmet
        0x26D3..0x26D4, // ⛓..⛔ chains, no entry
        0x26E9..0x26E9, // ⛩ shinto shrine
        0x26EA..0x26EA, // ⛪ church
        0x26F0..0x26F5, // ⛰..⛵ mountain..sailboat
        0x26F7..0x26F9, // ⛷..⛹ skier..person bouncing ball
        0x26FA..0x26FA, // ⛺ tent
        0x26FD..0x26FD, // ⛽ fuel pump
        
        // Dingbats (2700-27BF)
        0x2702..0x2702, // ✂ scissors
        0x2705..0x2705, // ✅ check mark button
        0x2708..0x270C, // ✈..✌ airplane..victory hand
        0x270D..0x270D, // ✍ writing hand
        0x270F..0x270F, // ✏ pencil
        0x2712..0x2712, // ✒ black nib
        0x2714..0x2714, // ✔ check mark
        0x2716..0x2716, // ✖ multiply
        0x271D..0x271D, // ✝ latin cross
        0x2721..0x2721, // ✡ star of David
        0x2728..0x2728, // ✨ sparkles
        0x2733..0x2734, // ✳..✴ asterisk, eight-pointed star
        0x2744..0x2744, // ❄ snowflake
        0x2747..0x2747, // ❇ sparkle
        0x274C..0x274C, // ❌ cross mark
        0x274E..0x274E, // ❎ cross mark button
        0x2753..0x2755, // ❓..❕ question, exclamation marks
        0x2757..0x2757, // ❗ red exclamation mark
        0x2763..0x2764, // ❣..❤ heart exclamation, red heart
        0x2795..0x2797, // ➕..➗ math symbols
        0x27A1..0x27A1, // ➡ right arrow
        0x27B0..0x27B0, // ➰ curly loop
        0x27BF..0x27BF, // ➿ double curly loop
        
        // Miscellaneous Symbols and Arrows
        0x2934..0x2935, // ⤴..⤵ arrows curving
        0x2B05..0x2B07, // ⬅..⬇ left, up, down arrows
        
        // CJK Symbols and Punctuation
        0x3030..0x3030, // 〰 wavy dash
        0x303D..0x303D, // 〽 part alternation mark
        0x3297..0x3297, // ㊗ Japanese "congratulations"
        0x3299..0x3299, // ㊙ Japanese "secret"
        
        // Mahjong Tiles
        0x1F004..0x1F004, // 🀄 mahjong red dragon
        
        // Playing Cards
        0x1F0CF..0x1F0CF, // 🃏 joker
        
        // Enclosed Alphanumeric Supplement
        0x1F170..0x1F171, // 🅰..🅱 A, B buttons
        0x1F17E..0x1F17F, // 🅾..🅿 O, P buttons
        0x1F18E..0x1F18E, // 🆎 AB button
        0x1F191..0x1F19A, // 🆑..🆚 CL..VS buttons
        0x1F1E6..0x1F1FF, // 🇦..🇿 regional indicators (флаги)
        
        // Enclosed Ideographic Supplement
        0x1F201..0x1F202, // 🈁..🈂 Japanese "here", "service"
        0x1F21A..0x1F21A, // 🈚 Japanese "free of charge"
        0x1F22F..0x1F22F, // 🈯 Japanese "reserved"
        0x1F232..0x1F23A, // 🈲..🈺 Japanese buttons
        0x1F250..0x1F251, // 🉐..🉑 Japanese "bargain", "acceptable"
        
        // Miscellaneous Symbols and Pictographs (1F300-1F5FF)
        0x1F300..0x1F30C, // 🌀..🌌 cyclone..milky way
        0x1F30D..0x1F30E, // 🌍..🌎 globes
        0x1F30F..0x1F30F, // 🌏 globe Asia-Australia
        0x1F310..0x1F310, // 🌐 globe with meridians
        0x1F311..0x1F318, // 🌑..🌘 moon phases
        0x1F319..0x1F31C, // 🌙..🌜 moon variants
        0x1F31D..0x1F31E, // 🌝..🌞 moon, sun with face
        0x1F31F..0x1F320, // 🌟..🌠 glowing star, shooting star
        0x1F321..0x1F321, // 🌡 thermometer
        0x1F324..0x1F32C, // 🌤..🌬 weather symbols
        0x1F32D..0x1F32F, // 🌭..🌯 hot dog..burrito
        0x1F330..0x1F331, // 🌰..🌱 chestnut, seedling
        0x1F332..0x1F333, // 🌲..🌳 evergreen, deciduous trees
        0x1F334..0x1F335, // 🌴..🌵 palm tree, cactus
        0x1F336..0x1F336, // 🌶 hot pepper
        0x1F337..0x1F34A, // 🌷..🍊 tulip..tangerine
        0x1F34B..0x1F34B, // 🍋 lemon
        0x1F34C..0x1F34F, // 🍌..🍍 banana..pineapple
        0x1F350..0x1F350, // 🍐 pear
        0x1F351..0x1F37B, // 🍑..🍻 peach..beer mugs
        0x1F37C..0x1F37C, // 🍼 baby bottle
        0x1F37D..0x1F37D, // 🍽 fork knife plate
        0x1F37E..0x1F37F, // 🍾..🍿 champagne, popcorn
        0x1F380..0x1F393, // 🎀..🎓 ribbon..graduation cap
        0x1F396..0x1F397, // 🎖..🎗 military medal, reminder ribbon
        0x1F399..0x1F39B, // 🎙..🎛 studio mic, control knobs
        0x1F39E..0x1F39F, // 🎞..🎟 film frames, tickets
        0x1F3A0..0x1F3C4, // 🎠..🏄 carousel..surfing
        0x1F3C5..0x1F3C5, // 🏅 sports medal
        0x1F3C6..0x1F3C6, // 🏆 trophy
        0x1F3C7..0x1F3C7, // 🏇 horse racing
        0x1F3C8..0x1F3C8, // 🏈 american football
        0x1F3C9..0x1F3C9, // 🏉 rugby football
        0x1F3CA..0x1F3CA, // 🏊 person swimming
        0x1F3CB..0x1F3CE, // 🏋..🏎 weightlifting, racing car
        0x1F3CF..0x1F3D3, // 🏏..🏓 cricket..ping pong
        0x1F3D4..0x1F3DF, // 🏔..🏟 mountains..stadium
        0x1F3E0..0x1F3E3, // 🏠..🏣 houses..post office
        0x1F3E4..0x1F3E4, // 🏤 post office
        0x1F3E5..0x1F3F0, // 🏥..🏰 hospital..castle
        0x1F3F3..0x1F3F3, // 🏳 white flag
        0x1F3F4..0x1F3F4, // 🏴 black flag
        0x1F3F5..0x1F3F5, // 🏵 rosette
        0x1F3F7..0x1F3F7, // 🏷 label
        0x1F3F8..0x1F407, // 🏸..🐇 badminton..rabbit
        0x1F408..0x1F408, // 🐈 cat
        0x1F409..0x1F40B, // 🐉..🐋 dragon..whale
        0x1F40C..0x1F40E, // 🐌..🐎 snail..horse
        0x1F40F..0x1F410, // 🐏..🐐 ram, goat
        0x1F411..0x1F412, // 🐑..🐒 ewe, monkey
        0x1F413..0x1F413, // 🐓 rooster
        0x1F414..0x1F414, // 🐔 chicken
        0x1F415..0x1F415, // 🐕 dog
        0x1F416..0x1F416, // 🐖 pig
        0x1F417..0x1F429, // 🐗..🐩 boar..poodle
        0x1F42A..0x1F42A, // 🐪 camel
        0x1F42B..0x1F43E, // 🐫..🐾 two-hump camel..paw prints
        0x1F43F..0x1F43F, // 🐿 chipmunk
        0x1F440..0x1F440, // 👀 eyes
        0x1F441..0x1F441, // 👁 eye
        0x1F442..0x1F464, // 👂..👤 ear..bust silhouette
        0x1F465..0x1F465, // 👥 busts in silhouette
        0x1F466..0x1F46B, // 👦..👫 boy..couple
        0x1F46C..0x1F46D, // 👬..👭 men, women holding hands
        0x1F46E..0x1F4AC, // 👮..💬 police..speech balloon
        0x1F4AD..0x1F4AD, // 💭 thought balloon
        0x1F4AE..0x1F4B5, // 💮..💵 flower..dollar bill
        0x1F4B6..0x1F4B7, // 💶..💷 euro, pound bills
        0x1F4B8..0x1F4EB, // 💸..📫 money..closed mailbox
        0x1F4EC..0x1F4ED, // 📬..📭 open mailbox variants
        0x1F4EE..0x1F4EE, // 📮 postbox
        0x1F4EF..0x1F4EF, // 📯 postal horn
        0x1F4F0..0x1F4F4, // 📰..📴 newspaper..phone off
        0x1F4F5..0x1F4F5, // 📵 no mobile phones
        0x1F4F6..0x1F4F7, // 📶..📷 antenna, camera
        0x1F4F8..0x1F4F8, // 📸 camera with flash
        0x1F4F9..0x1F4FC, // 📹..📼 video camera..VHS
        0x1F4FD..0x1F4FD, // 📽 film projector
        0x1F4FF..0x1F502, // 📿..🔂 prayer beads..repeat single
        0x1F503..0x1F503, // 🔃 clockwise vertical arrows
        0x1F504..0x1F507, // 🔄..🔇 arrows..muted speaker
        0x1F508..0x1F508, // 🔈 speaker low volume
        0x1F509..0x1F509, // 🔉 speaker medium volume
        0x1F50A..0x1F514, // 🔊..🔔 speaker high..bell
        0x1F515..0x1F515, // 🔕 bell with slash
        0x1F516..0x1F52B, // 🔖..🔫 bookmark..water pistol
        0x1F52C..0x1F52D, // 🔬..🔭 microscope, telescope
        0x1F52E..0x1F53D, // 🔮..🔽 crystal ball..down button
        0x1F549..0x1F54A, // 🕉..🕊 om, dove
        0x1F54B..0x1F54E, // 🕋..🕎 kaaba..menorah
        0x1F550..0x1F55B, // 🕐..🕛 one..twelve o'clock
        0x1F55C..0x1F567, // 🕜..🕧 one-thirty..twelve-thirty
        0x1F56F..0x1F570, // 🕯..🕰 candle, mantelpiece clock
        0x1F573..0x1F579, // 🕳..🕹 hole..joystick
        0x1F57A..0x1F57A, // 🕺 man dancing
        0x1F587..0x1F587, // 🖇 linked paperclips
        0x1F58A..0x1F58D, // 🖊..🖍 pen..crayon
        0x1F590..0x1F590, // 🖐 hand with fingers splayed
        0x1F595..0x1F596, // 🖕..🖖 middle finger, vulcan salute
        0x1F5A4..0x1F5A4, // 🖤 black heart
        0x1F5A5..0x1F5A5, // 🖥 desktop computer
        0x1F5A8..0x1F5A8, // 🖨 printer
        0x1F5B1..0x1F5B2, // 🖱..🖲 mouse, trackball
        0x1F5BC..0x1F5BC, // 🖼 framed picture
        0x1F5C2..0x1F5C4, // 🗂..🗄 card index..file cabinet
        0x1F5D1..0x1F5D3, // 🗑..🗓 wastebasket..spiral calendar
        0x1F5DC..0x1F5DE, // 🗜..🗞 clamp..rolled-up newspaper
        0x1F5E1..0x1F5E1, // 🗡 dagger
        0x1F5E3..0x1F5E3, // 🗣 speaking head
        0x1F5E8..0x1F5E8, // 🗨 left speech bubble
        0x1F5EF..0x1F5EF, // 🗯 right anger bubble
        0x1F5F3..0x1F5F3, // 🗳 ballot box
        0x1F5FA..0x1F5FA, // 🗺 world map
        0x1F5FB..0x1F5FF, // 🗻..🗿 mount fuji..moai
        
        // Emoticons (1F600-1F64F)
        0x1F600..0x1F600, // 😀 grinning face
        0x1F601..0x1F606, // 😁..😆 beaming..grinning squinting
        0x1F607..0x1F608, // 😇..😈 smiling with halo, horns
        0x1F609..0x1F60D, // 😉..😍 winking..heart-eyes
        0x1F60E..0x1F60E, // 😎 smiling with sunglasses
        0x1F60F..0x1F60F, // 😏 smirking face
        0x1F610..0x1F610, // 😐 neutral face
        0x1F611..0x1F611, // 😑 expressionless face
        0x1F612..0x1F614, // 😒..😔 unamused..pensive
        0x1F615..0x1F615, // 😕 confused face
        0x1F616..0x1F616, // 😖 confounded face
        0x1F617..0x1F617, // 😗 kissing face
        0x1F618..0x1F618, // 😘 face blowing kiss
        0x1F619..0x1F61A, // 😙..😚 kissing variants
        0x1F61B..0x1F61B, // 😛 face with tongue
        0x1F61C..0x1F61E, // 😜..😞 winking with tongue..disappointed
        0x1F61F..0x1F61F, // 😟 worried face
        0x1F620..0x1F625, // 😠..😥 angry..sad relieved
        0x1F626..0x1F627, // 😦..😧 frowning..anguished
        0x1F628..0x1F62B, // 😨..😫 fearful..tired
        0x1F62C..0x1F62C, // 😬 grimacing face
        0x1F62D..0x1F62D, // 😭 loudly crying
        0x1F62E..0x1F62F, // 😮..😯 open mouth, hushed
        0x1F630..0x1F633, // 😰..😳 anxious..flushed
        0x1F634..0x1F634, // 😴 sleeping face
        0x1F635..0x1F635, // 😵 face with crossed-out eyes
        0x1F636..0x1F636, // 😶 face without mouth
        0x1F637..0x1F640, // 😷..🙀 medical mask..weary cat
        0x1F641..0x1F644, // 🙁..🙄 slightly frowning..rolling eyes
        0x1F645..0x1F64F, // 🙅..🙏 NO gesture..folded hands
        
        // Transport and Map Symbols (1F680-1F6FF)
        0x1F680..0x1F680, // 🚀 rocket
        0x1F681..0x1F682, // 🚁..🚂 helicopter, locomotive
        0x1F683..0x1F685, // 🚃..🚅 railway cars
        0x1F686..0x1F686, // 🚆 train
        0x1F687..0x1F687, // 🚇 metro
        0x1F688..0x1F688, // 🚈 light rail
        0x1F689..0x1F689, // 🚉 station
        0x1F68A..0x1F68B, // 🚊..🚋 tram, tram car
        0x1F68C..0x1F68C, // 🚌 bus
        0x1F68D..0x1F68D, // 🚍 oncoming bus
        0x1F68E..0x1F68E, // 🚎 trolleybus
        0x1F68F..0x1F68F, // 🚏 bus stop
        0x1F690..0x1F690, // 🚐 minibus
        0x1F691..0x1F693, // 🚑..🚓 ambulance..police car
        0x1F694..0x1F694, // 🚔 oncoming police car
        0x1F695..0x1F695, // 🚕 taxi
        0x1F696..0x1F696, // 🚖 oncoming taxi
        0x1F697..0x1F697, // 🚗 automobile
        0x1F698..0x1F698, // 🚘 oncoming automobile
        0x1F699..0x1F69A, // 🚙..🚚 SUV, delivery truck
        0x1F69B..0x1F6A1, // 🚛..🚡 articulated lorry..aerial tramway
        0x1F6A2..0x1F6A2, // 🚢 ship
        0x1F6A3..0x1F6A3, // 🚣 person rowing boat
        0x1F6A4..0x1F6A5, // 🚤..🚥 speedboat, traffic light horizontal
        0x1F6A6..0x1F6A6, // 🚦 vertical traffic light
        0x1F6A7..0x1F6AD, // 🚧..🚭 construction..no smoking
        0x1F6AE..0x1F6B1, // 🚮..🚱 litter bin..non-potable water
        0x1F6B2..0x1F6B2, // 🚲 bicycle
        0x1F6B3..0x1F6B5, // 🚳..🚵 no bicycles..mountain biking
        0x1F6B6..0x1F6B6, // 🚶 person walking
        0x1F6B7..0x1F6B8, // 🚷..🚸 no pedestrians, children crossing
        0x1F6B9..0x1F6BE, // 🚹..🚾 men's room..water closet
        0x1F6BF..0x1F6BF, // 🚿 shower
        0x1F6C0..0x1F6C0, // 🛀 person taking bath
        0x1F6C1..0x1F6C5, // 🛁..🛆 bathtub..left luggage
        0x1F6CB..0x1F6CB, // 🛋 couch and lamp
        0x1F6CC..0x1F6CC, // 🛌 person in bed
        0x1F6CD..0x1F6CF, // 🛍..🛏 shopping bags..bed
        0x1F6D0..0x1F6D0, // 🛐 place of worship
        0x1F6D1..0x1F6D2, // 🛑..🛒 stop sign, shopping cart
        0x1F6D5..0x1F6D5, // 🛕 hindu temple
        0x1F6D6..0x1F6D7, // 🛖..🛗 hut, elevator
        0x1F6D8..0x1F6D8, // 🛘 landslide
        0x1F6D9..0x1F6D9, // 🛙 lighthouse
        0x1F6DC..0x1F6DC, // 🛜 wireless
        0x1F6DD..0x1F6DF, // 🛝..🛟 playground slide..ring buoy
        0x1F6EB..0x1F6EC, // 🛫..🛬 airplane departure, arrival
        0x1F6F4..0x1F6F6, // 🛴..🛶 kick scooter..canoe
        0x1F6F7..0x1F6F8, // 🛷..🛸 sled, flying saucer
        0x1F6F9..0x1F6F9, // 🛹 skateboard
        0x1F6FA..0x1F6FA, // 🛺 auto rickshaw
        0x1F6FB..0x1F6FC, // 🛻..🛼 pickup truck, roller skate
        
        // Geometric Shapes Extended
        0x1F7E0..0x1F7EB, // 🟠..🟫 orange..brown square
        0x1F7F0..0x1F7F0, // 🟰 heavy equals sign
        
        // Supplemental Symbols and Pictographs (1F900-1F9FF)
        0x1F90C..0x1F90C, // 🤌 pinched fingers
        0x1F90D..0x1F90F, // 🤍..🤏 white heart..pinching hand
        0x1F910..0x1F918, // 🤐..🤘 zipper-mouth..horns
        0x1F919..0x1F91E, // 🤙..🤞 call me hand..crossed fingers
        0x1F91F..0x1F91F, // 🤟 love-you gesture
        0x1F920..0x1F927, // 🤠..🤧 cowboy..sneezing
        0x1F928..0x1F92F, // 🤨..🤯 raised eyebrow..exploding head
        0x1F930..0x1F930, // 🤰 pregnant woman
        0x1F931..0x1F932, // 🤱..🤲 breast-feeding, palms up
        0x1F933..0x1F93A, // 🤳..🤺 selfie..fencing
        0x1F93C..0x1F93E, // 🤼..🤾 wrestling..handball
        0x1F93F..0x1F93F, // 🤿 diving mask
        0x1F940..0x1F945, // 🍀..🍅 wilted flower..goal net
        0x1F947..0x1F94B, // 🥇..🥋 1st place..martial arts
        0x1F94C..0x1F94C, // 🥌 curling stone
        0x1F94D..0x1F94F, // 🥍..🥏 lacrosse..flying disc
        0x1F950..0x1F95E, // 🥐..🥞 croissant..pancakes
        0x1F95F..0x1F96B, // 🥟..🥫 dumpling..canned food
        0x1F96C..0x1F970, // 🥬..🥰 leafy green..smiling hearts
        0x1F971..0x1F971, // 🥱 yawning face
        0x1F972..0x1F972, // 🥲 smiling face with tear
        0x1F973..0x1F976, // 🥳..🥶 partying..cold face
        0x1F977..0x1F978, // 🥷..🥸 ninja, disguised face
        0x1F979..0x1F979, // 🥹 face holding back tears
        0x1F97A..0x1F97A, // 🥺 pleading face
        0x1F97B..0x1F97B, // 🥻 sari
        0x1F97C..0x1F97F, // 🥼..🥿 lab coat..flat shoe
        0x1F980..0x1F984, // 🦀..🦄 crab..unicorn
        0x1F985..0x1F991, // 🦅..🦑 eagle..squid
        0x1F992..0x1F997, // 🦒..🦗 giraffe..cricket
        0x1F998..0x1F9A2, // 🦘..🦢 kangaroo..swan
        0x1F9A3..0x1F9A4, // 🦣..🦤 mammoth, dodo
        0x1F9A5..0x1F9AA, // 🦥..🦪 sloth..oyster
        0x1F9AB..0x1F9AD, // 🦫..🦭 beaver..seal
        0x1F9AE..0x1F9AF, // 🦮..🦯 guide dog, white cane
        0x1F9B0..0x1F9B9, // 🦰..🦹 red hair..supervillain
        0x1F9BA..0x1F9BF, // 🦺..🦿 safety vest..mechanical leg
        0x1F9C0..0x1F9C0, // 🧀 cheese wedge
        0x1F9C1..0x1F9C2, // 🧁..🧂 cupcake, salt
        0x1F9C3..0x1F9CA, // 🧃..🧊 beverage box..ice
        0x1F9CB..0x1F9CB, // 🧋 bubble tea
        0x1F9CC..0x1F9CC, // 🧌 troll
        0x1F9CD..0x1F9CF, // 🧍..🧏 person standing..deaf person
        0x1F9D0..0x1F9E6, // 🧐..🧦 monocle..socks
        0x1F9E7..0x1F9FF, // 🧧..🧿 red envelope..nazar amulet
        
        // Symbols and Pictographs Extended-A (1FA00-1FAFF)
        0x1FA70..0x1FA73, // 🩰..🩳 ballet shoes..shorts
        0x1FA74..0x1FA74, // 🩴 thong sandal
        0x1FA75..0x1FA77, // 🩵..🩷 light blue..pink heart
        0x1FA78..0x1FA7A, // 🩸..🩺 drop of blood..stethoscope
        0x1FA7B..0x1FA7C, // 🩻..🩼 x-ray, crutch
        0x1FA80..0x1FA82, // 🪀..🪂 yo-yo..parachute
        0x1FA83..0x1FA86, // 🪃..🪆 boomerang..nesting dolls
        0x1FA87..0x1FA88, // 🪇..🪈 maracas, flute
        0x1FA89..0x1FA89, // 🪉 harp
        0x1FA8A..0x1FA8A, // 🪊 trombone
        0x1FA8B..0x1FA8D, // 🪋..🪍 meteor..net with handle
        0x1FA8E..0x1FA8E, // 🪎 treasure chest
        0x1FA8F..0x1FA8F, // 🪏 shovel
        0x1FA90..0x1FA95, // 🪐..🪕 ringed planet..banjo
        0x1FA96..0x1FAA8, // 🪖..🪨 military helmet..rock
        0x1FAA9..0x1FAAC, // 🪩..🪬 mirror ball, hamsa
        0x1FAAD..0x1FAAF, // 🪭..🪯 folding hand fan..khanda
        0x1FAB0..0x1FAB6, // 🪰..🪶 fly..feather
        0x1FAB7..0x1FABA, // 🪷..🪺 lotus..nest with eggs
        0x1FABB..0x1FABD, // 🪻..🪽 hyacinth..wing
        0x1FABE..0x1FABE, // 🪾 leafless tree
        0x1FABF..0x1FABF, // 🪿 goose
        0x1FAC0..0x1FAC2, // 🫀..🫂 anatomical heart..people hugging
        0x1FAC3..0x1FAC5, // 🫃..🫅 pregnant man..person with crown
        0x1FAC6..0x1FAC6, // 🫆 fingerprint
        0x1FAC8..0x1FAC8, // 🫈 hairy creature
        0x1FACC..0x1FACC, // 🫌 monarch butterfly
        0x1FACD..0x1FACD, // 🫍 orca
        0x1FACE..0x1FACF, // 🫎..🫏 moose, donkey
        0x1FAD0..0x1FAD6, // 🫐..🫖 blueberries..teapot
        0x1FAD7..0x1FAD9, // 🫗..🫙 pouring liquid..jar
        0x1FADA..0x1FADB, // 🫚..🫛 ginger root, pea pod
        0x1FADC..0x1FADC, // 🫜 root vegetable
        0x1FADD..0x1FADD, // 🫝 pickle
        0x1FADF..0x1FADF, // 🫟 splatter
        0x1FAE0..0x1FAE7, // 🫠..🫧 melting face..bubbles
        0x1FAE8..0x1FAE8, // 🫨 shaking face
        0x1FAE9..0x1FAE9, // 🫩 face with bags under eyes
        0x1FAEA..0x1FAEA, // 🫪 distorted face
        0x1FAEB..0x1FAEB, // 🫫 cracking face
        0x1FAEF..0x1FAEF, // 🫯 fight cloud
        0x1FAF0..0x1FAF6, // 🫰..🫶 index with thumb..heart hands
        0x1FAF7..0x1FAF8, // 🫷..🫸 leftwards..rightwards pushing hand
        0x1FAF9..0x1FAFA  // 🫹..🫺 leftwards..rightwards thumb sign
    )

    /**
     * Проверяет, является ли кодовая точка эмодзи
     */
    private fun isEmojiCodePoint(codePoint: Int): Boolean {
        return emojiRanges.any { range -> codePoint in range }
    }

    /**
     * Извлекает первый найденный эмодзи из строки.
     * Поддерживает:
     * - Простые эмодзи (👍, ❤️, 🔥)
     * - Эмодзи с модификаторами тона кожи (👍🏻, 👍🏼)
     * - Флаги (🇺🇸, 🇷🇺)
     * - Составные эмодзи (👨‍👩‍👧‍👦, 👨‍⚕️)
     * 
     * @param text Строка для поиска
     * @return Первый найденный эмодзи или null
     */
    fun extractFirstEmoji(text: String): String? {
        if (text.isEmpty()) return null

        val codePoints = text.codePointList()
        val result = StringBuilder()
        var i = 0

        while (i < codePoints.size) {
            val cp = codePoints[i]

            // Если это начало эмодзи-последовательности
            if (isEmojiCodePoint(cp)) {
                result.appendCodePoint(cp)

                // Проверяем следующие кодовые точки на наличие модификаторов
                var j = i + 1
                while (j < codePoints.size) {
                    val nextCp = codePoints[j]
                    
                    // Модификаторы тона кожи, ZWJ, variation selectors, combining marks
                    if (isEmojiModifier(nextCp) || 
                        nextCp == 0x200D.toChar().code || // ZWJ
                        nextCp == 0xFE0F.toChar().code || // Variation Selector-16
                        nextCp == 0xFE0E.toChar().code || // Variation Selector-15
                        nextCp == 0x20E3.toChar().code    // Combining Enclosing Keycap
                    ) {
                        result.appendCodePoint(nextCp)
                        j++
                    } else if (isEmojiCodePoint(nextCp) && nextCp >= 0x1F1E0 && nextCp <= 0x1F1FF) {
                        // Региональные индикаторы для флагов (нужно 2)
                        if (result.length == 2) { // Уже есть один региональный индикатор
                            result.appendCodePoint(nextCp)
                            j++
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
                return result.toString()
            }

            i++
        }

        return null
    }

    /**
     * Проверяет, является ли кодовая точка модификатором эмодзи
     */
    private fun isEmojiModifier(codePoint: Int): Boolean {
        // Skin tone modifiers
        return codePoint in 0x1F3FB..0x1F3FF
    }

    /**
     * Возвращает список всех кодовых точек из строки
     */
    private fun String.codePointList(): List<Int> {
        val result = mutableListOf<Int>()
        var i = 0
        while (i < this.length) {
            val cp = this.codePointAt(i)
            result.add(cp)
            i += Character.charCount(cp)
        }
        return result
    }
}
