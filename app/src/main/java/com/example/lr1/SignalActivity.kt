package com.example.lr1

import android.graphics.*
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

/**
 * SignalActivity - это второй экран, который отвечает за отображение сгенерированного сигнала и его спектра.
 */
class SignalActivity : AppCompatActivity() {

    // Объявляем переменные для элементов интерфейса: два "холста" для графиков и текстовое поле.
    private lateinit var signalImageView: ImageView
    private lateinit var spectrumImageView: ImageView
    private lateinit var paramsTextView: TextView

    /**
     * Метод `onCreate` вызывается при создании этого экрана.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Устанавливаем внешний вид из файла activity_signal.xml.
        setContentView(R.layout.activity_signal)

        // Связываем переменные с реальными элементами на экране.
        signalImageView = findViewById(R.id.signalImageView)
        spectrumImageView = findViewById(R.id.spectrumImageView)
        paramsTextView = findViewById(R.id.paramsTextView)

        // Достаем данные, которые были переданы с первого экрана (MainActivity).
        // У каждого значения есть ключ (например, "AMPLITUDE") и значение по умолчанию (например, 1.0f) на случай, если данные не придут.
        val amplitude = intent.getFloatExtra("AMPLITUDE", 1.0f)
        val frequency = intent.getFloatExtra("FREQUENCY", 1.0f)
        val phase = intent.getFloatExtra("PHASE", 0.0f)
        val points = intent.getIntExtra("POINTS", 1024)
        val duration = intent.getFloatExtra("DURATION", 1.0f)

        // Формируем и выводим строку с полученными параметрами в текстовое поле.
        paramsTextView.text = "Параметры: A=%.2f, f=%.2f, ph=%.2f, N=%d, T=%.2f".format(amplitude, frequency, phase, points, duration)

        // 1. Генерируем сигнал (массив значений) и временную шкалу для него.
        val (signal, time) = generateSignal(amplitude, frequency, phase, points, duration)
        // 2. Вычисляем частотный спектр для этого сигнала.
        val spectrum = calculateSpectrum(signal, points)
        // 3. Создаем шкалу частот для графика спектра.
        val frequencyAxis = DoubleArray(points / 2) { it * (1.0 / duration) }

        // 4. Вызываем нашу функцию для отрисовки двух графиков.
        drawGraph(signalImageView, time, signal, "Сигнал", "Время (c)", "Амплитуда", isBarChart = false) // Сигнал - как линия
        drawGraph(spectrumImageView, frequencyAxis, spectrum, "Спектр", "Частота (Гц)", "Амплитуда", isBarChart = true) // Спектр - как гистограмма
    }

    /**
     * Генерирует гармонический сигнал по заданной формуле.
     * @return Пара (Pair), содержащая два массива: сам сигнал и временные отсчеты.
     */
    private fun generateSignal(A: Float, f: Float, ph: Float, N: Int, T: Float): Pair<DoubleArray, DoubleArray> {
        val signal = DoubleArray(N) // Массив для значений амплитуды сигнала
        val time = DoubleArray(N)   // Массив для временных отметок
        val dt = T / (N - 1).toFloat() // Вычисляем шаг по времени

        // В цикле заполняем массивы по формуле гармонического сигнала.
        for (i in 0 until N) {
            time[i] = i * dt.toDouble()
            signal[i] = A * sin(2 * PI * f * time[i] + ph)
        }
        return Pair(signal, time)
    }

    /**
     * Вычисляет амплитудный спектр сигнала с помощью Быстрого Преобразования Фурье (БПФ).
     */
    private fun calculateSpectrum(signal: DoubleArray, N: Int): DoubleArray {
        val real = signal.clone() // Действительная часть (наш сигнал)
        val imag = DoubleArray(N)   // Мнимая часть (изначально нули)

        val fft = FFT(N) // Создаем наш объект для вычисления БПФ
        fft.fft(real, imag) // Выполняем преобразование

        // Вычисляем амплитуды для первой половины спектра (вторая половина - зеркальная).
        val spectrum = DoubleArray(N / 2)
        for (i in 0 until N / 2) {
            // Амплитуда вычисляется по теореме Пифагора для действительной и мнимой части.
            val magnitude = sqrt(real[i].pow(2) + imag[i].pow(2))
            // Нормализуем амплитуду.
            spectrum[i] = if (i == 0) magnitude / N else 2 * magnitude / N
        }
        return spectrum
    }

    /**
     * Главная функция отрисовки. Рисует график на заданном ImageView.
     */
    private fun drawGraph(imageView: ImageView, xData: DoubleArray?, yData: DoubleArray, title: String, xLabel: String, yLabel: String, isBarChart: Boolean) {
        // `post` выполняет код только после того, как ImageView будет отрисован и получит свои размеры (ширину и высоту).
        imageView.post {
            val width = imageView.width
            val height = imageView.height
            if (width == 0 || height == 0) return@post // Если размеров нет, рисовать негде.

            // Создаем пустое изображение (Bitmap), на котором будем рисовать.
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            // Создаем "холст" (Canvas), связанный с этим изображением.
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Заливаем фон белым цветом.

            // Настраиваем "кисти" для рисования.
            val paint = Paint().apply { isAntiAlias = true } // Основная кисть для линий.
            val textPaint = Paint().apply { // Кисть для текста.
                color = Color.BLACK
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val p = 80f // Внутренний отступ (padding) для области графика.

            // --- 1. Рисуем оси ---
            paint.color = Color.BLACK
            paint.strokeWidth = 3f
            canvas.drawLine(p, p, p, height - p, paint) // Вертикальная ось Y
            canvas.drawLine(p, height - p, width - p, height - p, paint) // Горизонтальная ось X

            // --- 2. Рисуем подписи ---
            canvas.drawText(title, width / 2f, p / 2, textPaint) // Заголовок графика
            canvas.drawText(xLabel, width / 2f, height - p / 4, textPaint) // Подпись оси X
            canvas.save() // Сохраняем текущее состояние холста (без поворота).
            canvas.rotate(-90f, p / 4, height / 2f) // Поворачиваем холст для текста.
            canvas.drawText(yLabel, p / 4, height / 2f, textPaint) // Рисуем подпись оси Y.
            canvas.restore() // Возвращаем холст в исходное (неповернутое) состояние.

            // --- 3. Рисуем данные ---
            // Находим максимальные и минимальные значения для масштабирования.
            val xMax = xData?.maxOrNull() ?: (yData.size - 1).toDouble()
            val yMax = yData.maxOrNull() ?: 1.0
            val yMin = if (isBarChart) 0.0 else (yData.minOrNull() ?: -1.0)
            val yRange = if (yMax > yMin) yMax - yMin else 1.0

            val plotWidth = width - 2 * p
            val plotHeight = height - 2 * p

            // В зависимости от флага `isBarChart` выбираем стиль рисования.
            if (isBarChart) { // Рисуем гистограмму (столбчатый график)
                paint.color = Color.RED
                paint.strokeWidth = max(1f, plotWidth / yData.size / 2) // Ширина столбца
                for (i in yData.indices) {
                    val xPos = p + (((xData?.get(i) ?: i.toDouble()) / xMax) * plotWidth).toFloat()
                    val yPos = (height - p) - ((yData[i] / yRange) * plotHeight).toFloat()
                    if (yData[i] > 0) { // Рисуем только ненулевые значения
                        canvas.drawLine(xPos, height - p, xPos, yPos, paint)
                    }
                }
            } else { // Рисуем линейный график
                paint.color = Color.BLUE
                paint.strokeWidth = 4f
                paint.style = Paint.Style.STROKE // Рисовать только контур, без заливки
                val path = Path() // Используем Path для рисования плавной линии по точкам.
                val xStart = p + (((xData?.get(0) ?: 0.0) / xMax) * plotWidth).toFloat()
                val yStart = (height - p) - (((yData[0] - yMin) / yRange) * plotHeight).toFloat()
                path.moveTo(xStart, yStart) // Перемещаемся в начальную точку.
                for (i in 1 until yData.size) {
                    val x = p + (((xData?.get(i) ?: i.toDouble()) / xMax) * plotWidth).toFloat()
                    val y = (height - p) - (((yData[i] - yMin) / yRange) * plotHeight).toFloat()
                    path.lineTo(x, y) // Рисуем линию к следующей точке.
                }
                canvas.drawPath(path, paint) // Отрисовываем весь путь.
            }
            // Устанавливаем наше готовое изображение (bitmap) в ImageView.
            imageView.setImageBitmap(bitmap)
        }
    }
}
