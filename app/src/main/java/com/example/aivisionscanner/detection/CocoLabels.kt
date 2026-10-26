package com.example.aivisionscanner.detection

object CocoLabels {

    private val names = listOf(
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
        "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
        "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
        "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
        "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
        "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
        "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
        "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    )

    private val indonesian = mapOf(
        "person" to "MANUSIA",
        "bicycle" to "SEPEDA",
        "car" to "MOBIL",
        "motorcycle" to "MOTOR",
        "bus" to "BUS",
        "truck" to "TRUK",
        "bird" to "BURUNG",
        "cat" to "KUCING",
        "dog" to "ANJING",
        "horse" to "KUDA",
        "sheep" to "DOMBA",
        "cow" to "SAPI",
        "elephant" to "GAJAH",
        "bear" to "BERUANG",
        "backpack" to "TAS RANSEL",
        "handbag" to "TAS",
        "umbrella" to "PAYUNG",
        "bottle" to "BOTOL",
        "cup" to "GELAS",
        "chair" to "KURSI",
        "couch" to "SOFA",
        "bed" to "KASUR",
        "dining table" to "MEJA",
        "tv" to "TV",
        "laptop" to "LAPTOP",
        "cell phone" to "HP",
        "book" to "BUKU",
        "potted plant" to "TANAMAN",
        "traffic light" to "LAMPU LALU LINTAS",
        "stop sign" to "RAMBU STOP",
        "clock" to "JAM"
    )

    fun nameOf(id: Int): String = names.getOrElse(id) { "class$id" }

    fun displayName(id: Int): String {
        val name = nameOf(id)
        return indonesian[name] ?: name.uppercase()
    }
}
