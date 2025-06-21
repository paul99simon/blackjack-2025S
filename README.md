# Hausaufgabe 5

## Aufgabe 3

### Header

<table>
<tr>
    <th>bytes</th>
    <th style="text-align:center;">1</th>
    <th style="text-align:center;">2</th>
    <th style="text-align:center;">3</th>
    <th style="text-align:center;">4</th>
    <th style="text-align:center;">5</th>
    <th style="text-align:center;">6</th>
    <th style="text-align:center;">7</th>
    <th style="text-align:center;">8</th>
</tr>
<tr>
    <th>1-8</th>
    <td colspan='4' style="text-align:center;">length</td>
    <td colspan='4' style="text-align:center;">message id</td>
</tr>
    <th>9-16</th>
    <td colspan='8' style="text-align:center;">sender id</td>
</tr>
<tr>
    <th>17-24</th>
    <td style="text-align:center;">mt* </td>
    <td style="text-align:center;">sr*</td>
    <td style="text-align:center;">0</td>
    <td style="text-align:center;">0</td>
    <td style="text-align:center;">0</td>
    <td style="text-align:center;">0</td>
    <td style="text-align:center;">0</td>
    <td style="text-align:center;">0</td>
</tr>
<tr>
    <th>25-4096</th>
    <td colspan='8' style="text-align:center;">Payload</td>
</tr>
</table>

- mt* = message type
- sr* = sender role

### Dealer  Player

### Dealer $\leftrightarrow$ Counter

### Counter $\leftrightarrow$ Player
