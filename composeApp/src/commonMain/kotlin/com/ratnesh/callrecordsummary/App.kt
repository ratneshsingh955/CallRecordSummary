package com.ratnesh.callrecordsummary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import callrecordsummary.composeapp.generated.resources.Res
import callrecordsummary.composeapp.generated.resources.apple_icon
import callrecordsummary.composeapp.generated.resources.google_icon
import callrecordsummary.composeapp.generated.resources.whatsApp_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview



@Composable
@Preview
fun LoginScreen() {
    Scaffold { paddingValues ->

        val radialGradient = Brush.radialGradient(
            colors = listOf(
                Color(0xFF00FF51).copy(alpha = 1f),
                Color(0xF00FF51).copy(alpha = 0.9f),
                Color(0xFF00FF51).copy(alpha = 0.8f),
                Color(0xFF2f2f2f).copy(alpha = 1f)
            ),
            radius = 500f,
            center = Offset(0.5f, 0.5f)
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFF2f2f2f).copy(alpha = 1f)) // Dark background color
        ) {
            Box( modifier = Modifier
                .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,

                ) {
                Column (modifier = Modifier.fillMaxWidth().background(Color(0xFF282828).copy(alpha = 0.8f), shape = RoundedCornerShape(30.dp,30.dp))
                    .border(2.dp, Color(0xFFFFFFFF).copy(alpha = 0.12f), shape = RoundedCornerShape(30.dp,30.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center)
                {

                    Row {
                        Text("Login/SignUp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(35.dp,20.dp,0.dp,10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.padding(8.dp))

                    GradientButton(
                        text = "Send Hi on WhatsApp",
                        onClick = { /* WhatsApp Kholna h */ },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.padding(15.dp))

                    // divider line with or in middle
                    OrDivider()

                    Spacer(modifier = Modifier.padding(5.dp))

                    googleAndAppleLogin()

                    Spacer(modifier = Modifier.padding(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        CustomCheckbox()
                        Text("  I accept your terms & conditions and privacy policy",
                            color = Color(0xFFFFFFFF).copy(alpha = 0.7f),
                            fontSize = 13.sp)
                    }

                    }

                }
            }
        }
    }

@Composable
fun CustomCheckbox() {
        var isChecked by remember { mutableStateOf(false) }
            Checkbox(
                modifier = Modifier.border(2.dp, color = if (isChecked) Color(0xFFCFCBCB) else Color.Gray, shape = RoundedCornerShape(4.dp))
                    .size(15.dp),
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFDCDCDC).copy(alpha = 0.7f),
                    uncheckedColor = Color.Transparent,
                    checkmarkColor = Color(0xFF000000)
                ),
            )
}


//@Composable
//fun CustomCheckbox() {
//    var isChecked by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .size(15.dp)
//            .border(
//                width = 2.dp,
//                color = if (isChecked) Color(0xFFCFCBCB) else Color.Gray,
//                shape = RoundedCornerShape(4.dp)
//            )
//            .background(
//                color = if (isChecked) Color(0xFFDCDCDC).copy(alpha = 0.7f) else Color.Transparent,
//                shape = RoundedCornerShape(4.dp)
//            )
//            .clickable { isChecked = !isChecked },
//        contentAlignment = Alignment.Center
//    ) {
//        Checkbox(
//            checked = isChecked,
//            onCheckedChange = { isChecked = it },
//            colors = CheckboxDefaults.colors(
//                checkedColor = Color.Transparent,
//                uncheckedColor = Color.Transparent,
//                checkmarkColor = Color.Black
//            ),
//            modifier = Modifier.size(10.dp)
//        )
//    }
//}




@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    // Define your gradient colors
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF079017),
            Color(0xFF054E05)
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, 150f)
    )


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(brush = gradient, shape = CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .padding(horizontal = 1.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = CircleShape,
            modifier = Modifier
                .matchParentSize(),
            contentPadding = PaddingValues()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.whatsApp_icon),
                    contentDescription = "WhatsApp Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun googleAndAppleLogin(){

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ){
        OutlinedIconButton(
            onClick = { /* Google se login karna h */ },
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                width = 0.dp,

                ),
            colors = IconButtonColors(
                contentColor = Color.Blue,
                disabledContentColor = Color.Gray,
                containerColor = Color.White,
                disabledContainerColor = Color.White,

                ),
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .height(55.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(Res.drawable.google_icon),
                    contentDescription = "Google Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000).copy(alpha = 0.75f)
                )
            }
        }

        OutlinedIconButton(
            onClick = { /* Google se login karna h */ },
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                width = 0.dp,

                ),
            colors = IconButtonColors(
                contentColor = Color.Blue,
                disabledContentColor = Color.Gray,
                containerColor = Color.White,
                disabledContainerColor = Color.White,

                ),
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .height(55.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(Res.drawable.apple_icon),
                    contentDescription = "Apple Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apple",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000).copy(alpha = 0.75f)
                )
            }
        }

    }

}

@Composable
fun OrDivider() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        FadingDivider()

        Box(
            modifier = Modifier
                .background(Color(0xFF2d2e2d).copy(alpha = 1f), shape = CircleShape)
                .size(25.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Or",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}


@Composable
fun FadingDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .padding(horizontal = 60.dp)
    ) {
        // Draw a horizontal line with gradient fade
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,                           // start = fade in
                    Color(0xFFFFFFFF).copy(alpha = 0.32f),       // middle = visible
                    Color.Transparent  )                          // end = fade out

            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height
        )
    }
}

// Android-specific App function
@Composable
fun AppAndroid() {
    MaterialTheme {
        // This will be used by Android MainActivity
        Text("Android App - Call Recording Summary")
    }
}