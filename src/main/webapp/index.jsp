<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
    <style>
        .form-window {
            width: 500px;
            margin: 20px auto; /* centers horizontally */
            padding: 6px;
            border: 8px solid #ccc;
            border-radius: 20px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
            background-color: #f9f9f9;
        }

        h3 {
            text-align: center;
        }

        input, button {
            width: 100%;
            padding: 2px;
            margin: 2px 0;
            box-sizing: border-box;
            border: 2px solid #abc;
            border-radius: 10px;
            font-size: 14px;
            font-family: Arial, Helvetica, sans-serif;
        }

        input {
            background-color: #f9f9f9;
        }

        button, input[type="submit"] {
            background-color: #90EE90;
        }
    </style>
</head>
<body>
<h1><%= "test for currencyConverter!" %>
</h1>

<br/>
<a href="show-db">Show Currencies table in a readable view</a>

<br/>
<a href="currencies">All currencies JSON</a>
<br/>
<a href="exchangeRates">All exchange rates JSON</a>


<h3>Post exchange rate</h3>
<form class="form-window" action="exchangeRates" method="post" enctype="application/x-www-form-urlencoded">
    baseCurrencyCode: <input type="text" name="baseCurrencyCode"/><br><br>
    targetCurrencyCode: <input type="text" name="targetCurrencyCode"/><br><br>
    rate: <input type="text" name="rate"/><br><br>
    <input type="submit" value="Submit">
</form>

<h3>Post currency</h3>
<form class="form-window" action="currencies" method="post" enctype="application/x-www-form-urlencoded">
    name: <input type="text" name="name"/><br><br>
    code: <input type="text" name="code"/><br><br>
    sign: <input type="text" name="sign"/><br><br>
    <input type="submit" value="Submit">
</form>


</body>
</html>