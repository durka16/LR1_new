package com.example.lr1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity - это главный экран приложения.
 * Здесь пользователь вводит параметры для генерации сигнала.
 */
class MainActivity : AppCompatActivity() {

    // Объявляем переменные для всех элементов интерфейса (поля ввода и кнопка).
    // `lateinit` означает, что мы присвоим им значение позже (в `onCreate`).
    private lateinit var amplitudeEditText: EditText
    private lateinit var frequencyEditText: EditText
    private lateinit var phaseEditText: EditText
    private lateinit var pointsEditText: EditText
    private lateinit var durationEditText: EditText
    private lateinit var generateButton: Button

    /**
     * Метод `onCreate` вызывается при создании экрана.
     * Здесь происходит основная настройка: установка макета и обработчиков нажатий.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Устанавливаем, какой XML-файл будет определять внешний вид этого экрана.
        setContentView(R.layout.activity_main)

        // Находим реальные элементы интерфейса в макете по их ID и связываем с нашими переменными.
        amplitudeEditText = findViewById(R.id.amplitudeEditText)
        frequencyEditText = findViewById(R.id.frequencyEditText)
        phaseEditText = findViewById(R.id.phaseEditText)
        pointsEditText = findViewById(R.id.pointsEditText)
        durationEditText = findViewById(R.id.durationEditText)
        generateButton = findViewById(R.id.generateButton)

        // Устанавливаем обработчик нажатия на кнопку.
        // Код внутри этого блока `{}` выполнится, когда пользователь нажмет на кнопку.
        generateButton.setOnClickListener {
            // Сначала проверяем, все ли поля заполнены корректно.
            if (validateInput()) {
                // Если все в порядке, считываем текст из полей и преобразуем его в нужный числовой тип.
                val amplitude = amplitudeEditText.text.toString().toFloat()
                val frequency = frequencyEditText.text.toString().toFloat()
                val phase = phaseEditText.text.toString().toFloat()
                val points = pointsEditText.text.toString().toInt()
                val duration = durationEditText.text.toString().toFloat()

                // Создаем "намерение" (Intent) для перехода на второй экран (SignalActivity).
                val intent = Intent(this, SignalActivity::class.java).apply {
                    // С помощью `putExtra` кладем в это "намерение" наши данные.
                    // У каждого значения есть свой ключ (например, "AMPLITUDE"), по которому мы его потом достанем.
                    putExtra("AMPLITUDE", amplitude)
                    putExtra("FREQUENCY", frequency)
                    putExtra("PHASE", phase)
                    putExtra("POINTS", points)
                    putExtra("DURATION", duration)
                }
                // Запускаем второй экран, передавая ему intent с нашими данными.
                startActivity(intent)
            }
        }
    }

    /**
     * `validateInput` - наша собственная функция для проверки правильности ввода.
     * @return `true`, если все поля заполнены корректно, и `false` в противном случае.
     */
    private fun validateInput(): Boolean {
        var isValid = true // Заводим флаг, изначально считаем, что все хорошо.

        // Проверяем каждое поле: если текст из поля не может быть преобразован в число (результат `null`), то...
        if (amplitudeEditText.text.toString().toFloatOrNull() == null) {
            amplitudeEditText.error = "Введите корректное число" // ...показываем ошибку прямо у этого поля.
            isValid = false // ...и опускаем флаг.
        }

        if (frequencyEditText.text.toString().toFloatOrNull() == null) {
            frequencyEditText.error = "Введите корректное число"
            isValid = false
        }

        if (phaseEditText.text.toString().toFloatOrNull() == null) {
            phaseEditText.error = "Введите корректное число"
            isValid = false
        }

        if (pointsEditText.text.toString().toIntOrNull() == null) {
            pointsEditText.error = "Введите целое число"
            isValid = false
        }

        if (durationEditText.text.toString().toFloatOrNull() == null) {
            durationEditText.error = "Введите коррект-ное число"
            isValid = false
        }
        
        // Если после всех проверок флаг опустился (т.е. была хотя бы одна ошибка)...
        if (!isValid) {
            // ...показываем общее всплывающее сообщение внизу экрана.
            Toast.makeText(this, "Пожалуйста, исправьте ошибки ввода", Toast.LENGTH_SHORT).show()
        }

        // Возвращаем итоговый результат: true или false.
        return isValid
    }
}
