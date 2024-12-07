package com.dam2jms.gestiongastosapp.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dam2jms.gestiongastosapp.ui.theme.azul
import com.dam2jms.gestiongastosapp.ui.theme.blanco
import com.dam2jms.gestiongastosapp.ui.theme.colorFondo
import com.dam2jms.gestiongastosapp.ui.theme.grisClaro
import com.dam2jms.gestiongastosapp.ui.theme.naranjaClaro
import com.dam2jms.gestiongastosapp.ui.theme.rojo
import com.dam2jms.gestiongastosapp.ui.theme.verde
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficoBarras(ingresos: List<Double>, gastos: List<Double>, fechas: List<LocalDate>) {

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        factory = { context ->
            BarChart(context).apply {
                //quito la descripcion por defecto
                description.isEnabled = false
                //quito el fondo de cuadricula
                setDrawGridBackground(false)
                //quito el zoom
                setPinchZoom(false)
                //sin valores encima de las barras
                setDrawValueAboveBar(false)
                setBackgroundColor(colorFondo.toArgb())

                //configuracion del eje X
                xAxis.apply {
                    //posicion en la parte inferior
                    position = XAxis.XAxisPosition.BOTTOM
                    //sin lineas de cuadricula
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 12f
                    textColor = blanco.toArgb()
                }

                //animacion eje X
                animateX(1000)

                //configuracion del eje Y
                axisLeft.apply {
                    //lineas de cuadricula en el eje Y
                    setDrawGridLines(true)
                    //color de las lineas
                    gridColor = naranjaClaro.toArgb()
                    textSize = 12f
                    textColor = blanco.toArgb()
                    //dibujo la linea
                    setDrawAxisLine(true)
                    axisLineColor = naranjaClaro.toArgb()
                    //formateo los datos
                    valueFormatter = object : ValueFormatter(){
                        override fun getFormattedValue(valorDatos: Float): String {
                            return String.format("%.2f€", valorDatos)
                        }
                    }
                }

                //quito el eje Y derecho
                axisRight.isEnabled = false

                //configuracion de la leyenda
                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    //dibujo la leyenda fuera del grafico
                    setDrawInside(false)
                    textSize = 12f
                    formSize = 12f
                    textColor = blanco.toArgb()
                }

                //animacion del eje Y
                animateY(1000)
            }
        },
        update = { grafico ->

            //mapeo los ingresos y gastos a entradas para el grafico
            val entradasIngresos = ingresos.mapIndexed { index, valor ->
                BarEntry(index.toFloat(), valor.toFloat())
            }
            val entradasGastos = gastos.mapIndexed { index, valor ->
                BarEntry(index.toFloat(), valor.toFloat())
            }

            //configuro el conjunto de los ingresos que mostrara el grafico
            val datosIngresos = BarDataSet(entradasIngresos, "Ingresos").apply {
                color = verde.toArgb()
                valueTextSize = 12f
                valueTextColor = blanco.toArgb()
                valueFormatter = object : ValueFormatter(){
                    //formateo los datos
                    override fun getFormattedValue(valorIngresos: Float): String {
                        return String.format("%.2f€", valorIngresos)
                    }
                }
            }

            //configuro los datos de los gastos
            val datosGastos = BarDataSet(entradasGastos,"Gastos").apply {
                color = rojo.toArgb()
                valueTextSize = 12f
                valueTextColor = blanco.toArgb()
                //formateo los datos
                valueFormatter = object : ValueFormatter(){
                    override fun getFormattedValue(valorGastos: Float): String {
                        return String.format("%.2f€", valorGastos)
                    }
                }
            }

            //configuro las fechas
            val etiquetasFechas = fechas.map { fecha ->
                fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
            }
            grafico.xAxis.valueFormatter = IndexAxisValueFormatter(etiquetasFechas)

            //configuro los datos agrupados de las barras con el tamaño
            val datosBarras = BarData(datosIngresos, datosGastos).apply {
                barWidth = 0.35f
            }
            grafico.data = datosBarras

            grafico.groupBars(-0.5f, 0.06f, 0.02f)
            //ajusto las barras dentro del grafico
            grafico.setFitBars(true)
            //redibujo el grafico
            grafico.invalidate()
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GraficoLineas(datos: List<Double>, fechas: List<LocalDate>){

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setPinchZoom(false)
                setBackgroundColor(colorFondo.toArgb())

                //configuracion eje X
                xAxis.apply {
                    setDrawGridLines(true)
                    gridColor = naranjaClaro.toArgb()
                    textSize = 12f
                    textColor = blanco.toArgb()
                    axisLineColor = naranjaClaro.toArgb()
                }
                axisRight.isEnabled = false
                animateX(1000)

                //configuracion eje Y
                axisLeft.apply {
                    setDrawGridLines(false)
                    textSize = 12f
                    textColor = blanco.toArgb()
                    axisLineColor = naranjaClaro.toArgb()
                    valueFormatter = object : ValueFormatter(){
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f€", value)
                        }
                    }
                }
                animateY(1000)

                //configuracion de la leyenda
                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    textSize = 12f
                    formSize = 12f
                    textColor = blanco.toArgb()
                }
            }
        },
        update = { grafico ->

            //mapeo los datos a entradas
            val entradas = datos.mapIndexed { index, valor ->
                Entry(index.toFloat(), valor.toFloat())
            }

            //configuracion del conjunto de datos
            val conjuntoDatos = LineDataSet(entradas, "Evolucion balance").apply {
                color = naranjaClaro.toArgb()
                setCircleColors(blanco.toArgb())
                lineWidth = 2f
                //radio de los puntos de datos
                circleRadius = 4f
                //dibujo el espacio debajo de la linea
                setDrawFilled(true)
                //color del area debajo de la linea
                fillColor = naranjaClaro.toArgb()
                //transparencia del area debajo de la linea
                fillAlpha = 50
                //para suavizar la linea
                mode = LineDataSet.Mode.CUBIC_BEZIER
                valueTextSize = 10f
                valueTextColor = blanco.toArgb()
                valueFormatter = object : ValueFormatter(){
                    override fun getFormattedValue(valorDatos: Float): String {
                        return String.format("%.2f€", valorDatos)
                    }
                }
            }

            //configuro las fechas
            val etiquetasFechas = fechas.map { fecha ->
                fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
            }
            grafico.xAxis.valueFormatter = IndexAxisValueFormatter(etiquetasFechas)

            //pongo el conjunto de datos en el grafico
            grafico.data = LineData(conjuntoDatos)
            grafico.invalidate()
        }
    )
}

@Composable
fun GraficoCircularIngresos(datosIngresos: Map<String, Double>, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                //margenes internos
                setExtraOffsets(5f, 10f, 5f, 5f)
                setBackgroundColor(colorFondo.toArgb())

                //configuraciones del radio del circulo
                isDrawHoleEnabled = true
                setHoleColor(colorFondo.toArgb())
                setTransparentCircleColor(blanco.toArgb())
                setTransparentCircleAlpha(110)
                holeRadius = 40f
                transparentCircleRadius = 45f

                //configuraciones para la leyenda
                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    textSize = 10f
                    textColor = blanco.toArgb()
                }

                animateY(1000)
            }
        },
        update = { grafico ->

            //mapeo los datos en entradas para el grafico
            val entradas = datosIngresos.map { (categoria, valor) ->
                PieEntry(valor.toFloat(), categoria)
            }

            //junto los datos de ingresos y les doy un color a las categorias
            val conjuntoDatos = PieDataSet(entradas, "Ingresos").apply {
                this.colors = listOf(verde.toArgb(), azul.toArgb(), naranjaClaro.toArgb())
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueTextSize = 10f
                valueTextColor = blanco.toArgb()
                selectionShift = 5f
            }

            //formateo los datos
            val dato = PieData(conjuntoDatos).apply {
                setValueFormatter(PercentFormatter(grafico))
                setValueTextSize(10f)
                setValueTextColor(blanco.toArgb())
            }

            //pongo los datos en el grafico
            grafico.data = dato
            grafico.invalidate()
        }
    )
}

//lo mismo que el anterior pero para los datos de ingresos
@Composable
fun GraficoCircularGastos(datosGastos: Map<String, Double>, modifier: Modifier = Modifier) {

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setExtraOffsets(5f, 10f, 5f, 5f)
                setBackgroundColor(colorFondo.toArgb())

                isDrawHoleEnabled = true
                setHoleColor(colorFondo.toArgb())
                setTransparentCircleColor(blanco.toArgb())
                setTransparentCircleAlpha(110)
                holeRadius = 40f
                transparentCircleRadius = 45f

                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    //leyenda fuera del grafico
                    setDrawInside(false)
                    textSize = 10f
                    textColor = blanco.toArgb()
                }

                animateY(1000)
            }
        },
        update = { grafico ->

            //mapeo las entradas en datos
            val entradas = datosGastos.map { (categoria, valor) ->
                PieEntry(valor.toFloat(), categoria)
            }

            val conjuntoDatos = PieDataSet(entradas, "Gastos").apply {
                this.colors = listOf(rojo.toArgb(), grisClaro.toArgb(), naranjaClaro.toArgb())
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueTextSize = 10f
                valueTextColor = blanco.toArgb()
                selectionShift = 5f
            }

            val dato = PieData(conjuntoDatos).apply {
                setValueFormatter(PercentFormatter(grafico))
                setValueTextSize(10f)
                setValueTextColor(blanco.toArgb())
            }

            grafico.data = dato
            grafico.invalidate()
        }
    )
}

