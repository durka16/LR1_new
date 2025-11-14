package com.example.lr1

import kotlin.math.cos
import kotlin.math.sin

/**
 * Класс для выполнения Быстрого Преобразования Фурье (БПФ) по алгоритму Кули-Тьюки.
 * Это эффективный алгоритм для преобразования сигнала из временной области в частотную.
 */
class FFT(private val n: Int) { // `n` - количество точек, должно быть степенью двойки.

    // Предварительно вычисленные значения синусов и косинусов для ускорения расчетов.
    private val cosTable: DoubleArray
    private val sinTable: DoubleArray

    init { // Этот блок выполняется при создании объекта FFT.
        val m = n / 2
        cosTable = DoubleArray(m)
        sinTable = DoubleArray(m)
        // Заполняем таблицы значениями, которые будут многократно использоваться в алгоритме.
        for (i in 0 until m) {
            cosTable[i] = kotlin.math.cos(2 * Math.PI * i / n)
            sinTable[i] = kotlin.math.sin(2 * Math.PI * i / n)
        }
    }

    /**
     * Выполняет прямое БПФ над комплексным сигналом.
     * @param real Действительная часть сигнала (на входе - наш сигнал, на выходе - действительная часть спектра).
     * @param imag Мнимая часть сигнала (на входе - нули, на выходе - мнимая часть спектра).
     */
    fun fft(real: DoubleArray, imag: DoubleArray) {
        var i: Int
        var j: Int
        var k: Int
        var m: Int
        var tReal: Double // Временная переменная для действительной части
        var tImag: Double // Временная переменная для мнимой части

        // --- Этап 1: Перестановка с инверсией битов (Bit-reversal permutation) ---
        // Этот шаг переупорядочивает входные данные, чтобы основной цикл был эффективнее.
        i = 0
        j = 0
        while (i < n - 1) {
            if (i < j) {
                // Меняем местами элементы, если их индексы не совпадают после инверсии битов.
                tReal = real[i]
                tImag = imag[i]
                real[i] = real[j]
                imag[i] = imag[j]
                real[j] = tReal
                imag[j] = tImag
            }
            k = n / 2
            while (k <= j) {
                j -= k
                k /= 2
            }
            j += k
            i++
        }

        // --- Этап 2: Алгоритм Кули-Тьюки ---
        // Основной цикл, который выполняет преобразование, проходя по разным уровням ("бабочкам").
        m = 2 // Начинаем с групп по 2 элемента
        while (m <= n) { // Удваиваем размер группы на каждой итерации
            val hm = m / 2 // Половина размера группы
            i = 0
            while (i < n) {
                j = 0
                while (j < hm) {
                    k = j * n / m // Индекс для таблицы синусов/косинусов
                    
                    // "Бабочка" БПФ: вычисление для пары элементов.
                    val uReal = real[i + j + hm]
                    val uImag = imag[i + j + hm]
                    
                    // Поворачивающий множитель (twiddle factor) W_n^k = cos(2*pi*k/n) - i*sin(2*pi*k/n)
                    // Выполняем комплексное умножение: (uReal + i*uImag) * W_n^k
                    tReal = uReal * cosTable[k] - uImag * sinTable[k]
                    tImag = uReal * sinTable[k] + uImag * cosTable[k]
                    
                    // Вычисляем новые значения для элементов
                    real[i + j + hm] = real[i + j] - tReal
                    imag[i + j + hm] = imag[i + j] - tImag
                    real[i + j] += tReal
                    imag[i + j] += tImag
                    j++
                }
                i += m
            }
            m *= 2
        }
    }
}
