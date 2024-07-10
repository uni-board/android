package com.uniboard.help.presentation

import com.uniboard.R

object ItemsRepository {
    val items: List<ListItem> = listOf(
        ListItem(1, "Инструмент просмотра", R.drawable.visibility_icon, "Этот функция нужна для просматривания рабочей доски. Можно перемещаться по доске, приближать, отдалять", listOf(R.drawable.screen_visibility1, R.drawable.screen_visibility2)),
        ListItem(2, "Инструмент редактирования", R.drawable.edit_icon, "Это функция нужна для редактирования объектов доски. Можно изменять размер, положение, поворот объектов.", listOf(R.drawable.screen_edit1, R.drawable.screen_edit2)),
        ListItem(3, "Инструмент удаления", R.drawable.delete_icon, "Функция удаления объектов, путем нажатия на них.", listOf(R.drawable.screen_delete1, R.drawable.screen_delete2)),
        ListItem(4, "Инструмент рисования", R.drawable.draw_icon, "Функция рисования, выбирается цвет и толщина карандаша, проводя по экрану, создается линия, которая будет отдельным объектом", listOf(R.drawable.screen_draw1, R.drawable.screen_draw2)),
        ListItem(5, "Инструмент заметок", R.drawable.note_icon, "Функция для быстрого создания стикеров. Так же, как и в реальной жизни, они нужны для важных напоминаний", listOf(R.drawable.screen_note1, R.drawable.screen_note2)),
        ListItem(6, "Инструмент фигура", R.drawable.figure_icon, "функция для рисования геометрических фигур. Можно выбрать цвет, толщину, заполненность, тип фигуры. Чтобы нарисовать, необходимо провести от одной точки до другой по экрану.", listOf(R.drawable.screen_figure1, R.drawable.screen_figure2)),
        ListItem(7, "Инструмент текст", R.drawable.text_icon, "Функция для печати текста. Можно выбрать цвет текста", listOf(R.drawable.screen_text1, R.drawable.screen_text2)),
        ListItem(8, "Инструмент изображение", R.drawable.image_icon, "Функция вставки изображения. Пользователь выбирает изображение из телефона и оно вставляется на доску.", listOf(R.drawable.screen_image1, R.drawable.screen_image2)),
        ListItem(9, "Инструмент файл ", R.drawable.file_icon, "Функция вставки файла. Пользователь выбирает файл из телефона и оно вставляется на доску.", listOf(R.drawable.screen_file1, R.drawable.screen_file2)),
        ListItem(10, "Инструмент PDF", R.drawable.pdf_icon, "Функция вставки файла в PDF формате. Пользователь выбирает файл из телефона и оно вставляется на доску. Объект отображается в виде страниц файла.", listOf(R.drawable.screen_pdf1, R.drawable.screen_pdf2)),
        ListItem(11, "Инструмент скриншот", R.drawable.screenshot_icon, "Функция которая создает изображение доски, и предлагает сохранить его под определенным названием.", listOf(R.drawable.screen_screenshot1, R.drawable.screen_screenshot2))
        )
}