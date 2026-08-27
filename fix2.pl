#!/usr/bin/perl
use strict;
use warnings;

my $file = 'app/src/main/java/com/example/ui/screens/AgendaScreen.kt';
open my $in, '<', $file or die $!;
my @lines = <$in>;
close $in;

# We will look for "Box(modifier = Modifier.align(Alignment.BottomEnd)" and then make sure the braces above it are correct.
my $out_str = join("", @lines);

# Let's just remove the FAB and the end of the file, and re-construct it properly.
$out_str =~ s/                    item \{\s*Box\(modifier = Modifier\.fillMaxWidth\(\)\.padding\(16\.dp\)\.clickable \{ viewModel\.clearPaginationError\(\); viewModel\.loadMore\(context\) \}, contentAlignment = Alignment\.Center\) \{\s*Text\(text = "Error: \$paginationError \(Tap to retry\)", color = Color\.Red, fontSize = 14\.sp\)\s*\}\s*\}\s*\}.*//s;

my $rest = <<'END';
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { viewModel.clearPaginationError(); viewModel.loadMore(context) }, contentAlignment = Alignment.Center) {
                            Text(text = "Error: $paginationError (Tap to retry)", color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            }
        } // closes if-else

    } // closes Column
    
    // FAB inside BoxScope
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(24.dp)
            .size(64.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(VioraNeonLime)
            .bounceClick { /* Add meeting */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
    }
} // closes Box
} // closes AgendaScreen

@Composable
fun AgendaItem(item: AgendaItemData, onClick: () -> Unit = {}) {
    val alpha = if (item.isPast) 0.5f else 1f
    Row(
        modifier = Modifier.fillMaxWidth().bounceClick(enabled = !item.isPast) { onClick() }.alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rotated Text
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.day,
                style = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 84.sp,
                    fontFamily = com.example.ui.theme.SFProDisplayFontFamily,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    lineHeight = 60.sp,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                        alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Proportional,
                        trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                    )
                ),
                modifier = Modifier
                    .requiredWidth(120.dp)
                    .rotate(-90f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Vertical separator
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(100.dp)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (item.isOnline) VioraNeonLime else Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.type,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.time,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.title,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = VioraNeonLime,
            modifier = Modifier.size(32.dp)
        )
    }
}
END

open my $out, '>', $file or die $!;
print $out $out_str;
print $out $rest;
close $out;
