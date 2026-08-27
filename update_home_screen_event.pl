#!/usr/bin/perl
use strict;
use warnings;

my $file = 'app/src/main/java/com/example/ui/screens/HomeScreen.kt';
open my $in, '<', $file or die $!;
my @lines = <$in>;
close $in;

my $out_str = join("", @lines);

$out_str =~ s/val upcomingEvent by viewModel\.upcomingEvent\.collectAsState\(\)/val upcomingEvent by viewModel.upcomingEvent.collectAsState()\n    val agendaEvents by agendaViewModel.events.collectAsState()\n    val firstUpcomingAgendaEvent = agendaEvents.firstOrNull { !it.isPast }/;

$out_str =~ s/UpcomingEventCard\(\s*event = upcomingEvent,/val eventToDisplay = firstUpcomingAgendaEvent?.let {\n                                            val dateStr = it.originalDateTime\n                                            var monthStr = ""\n                                            try {\n                                                if (dateStr.length >= 10) {\n                                                    val d = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(dateStr.substring(0, 10))\n                                                    monthStr = java.text.SimpleDateFormat("MMM", java.util.Locale.US).format(d)\n                                                }\n                                            } catch(e: Exception) {}\n                                            com.example.model.CalendarEvent(day = it.day, month = monthStr, title = it.title, time = it.time)\n                                        } ?: upcomingEvent\n                                        UpcomingEventCard(\n                                            event = eventToDisplay,/;

open my $out, '>', $file or die $!;
print $out $out_str;
close $out;
