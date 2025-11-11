$(document).ready(function () {
    function getContextPath() {
        var path = window.location.pathname || "";
        var parts = path.split('/').filter(Boolean);
        return parts.length > 0 ? '/' + parts[0] : '';
    }

    const host = getContextPath();

    // Fetch the list of currencies and populate the select element
    function requestCurrencies() {
        $.ajax({
            url: `${host}/currencies`,
            type: "GET",
            dataType: "json",
            success: function (data) {
                const tbody = $('.currencies-table tbody');
                tbody.empty();
                $.each(data, function (index, currency) {
                    const row = $('<tr></tr>');
                    row.append($('<td></td>').text(currency.code));
                    row.append($('<td></td>').text(currency.name));
                    row.append($('<td></td>').text(currency.sign));
                    tbody.append(row);
                });

                const newRateBaseCurrency = $("#new-rate-base-currency");
                newRateBaseCurrency.empty();

                // populate the base currency select element with the list of currencies
                $.each(data, function (index, currency) {
                    newRateBaseCurrency.append(`<option value="${currency.code}">${currency.code}</option>`);
                });

                const newRateTargetCurrency = $("#new-rate-target-currency");
                newRateTargetCurrency.empty();

                // populate the target currency select element with the list of currencies
                $.each(data, function (index, currency) {
                    newRateTargetCurrency.append(`<option value="${currency.code}">${currency.code}</option>`);
                });

                const convertBaseCurrency = $("#convert-base-currency");
                convertBaseCurrency.empty();

                // populate the base currency select element with the list of currencies
                $.each(data, function (index, currency) {
                    convertBaseCurrency.append(`<option value="${currency.code}">${currency.code}</option>`);
                });

                const convertTargetCurrency = $("#convert-target-currency");
                convertTargetCurrency.empty();

                // populate the base currency select element with the list of currencies
                $.each(data, function (index, currency) {
                    convertTargetCurrency.append(`<option value="${currency.code}">${currency.code}</option>`);
                });
            },
            error: function (jqXHR, textStatus, errorThrown) {
                try {
                    const error = JSON.parse(jqXHR.responseText);
                    const toast = $('#api-error-toast');
                    $(toast).find('.toast-body').text(error.message || 'Request failed');
                    $(toast).toast('show');
                } catch (e) {
                    console.error('Request failed', jqXHR);
                }
            }
        });
    }

    requestCurrencies();

    $("#add-currency").submit(function (e) {
        e.preventDefault();

        $.ajax({
            url: `${host}/currencies`,
            type: "POST",
            data: $("#add-currency").serialize(),
            success: function (data) {
                requestCurrencies();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                try {
                    const error = JSON.parse(jqXHR.responseText);
                    const toast = $('#api-error-toast');
                    $(toast).find('.toast-body').text(error.message || 'Request failed');
                    $(toast).toast('show');
                } catch (e) {
                    console.error('Request failed', jqXHR);
                }
            }
        });

        return false;
    });

    function requestExchangeRates() {
        $.ajax({
            url: `${host}/exchangeRates`,
            type: "GET",
            dataType: "json",
            success: function (response) {
                const tbody = $('.exchange-rates-table tbody');
                tbody.empty();
                $.each(response, function (index, rate) {
                    const row = $('<tr></tr>');
                    const currency = rate.baseCurrency.code + rate.targetCurrency.code;
                    const exchangeRate = rate.rate;
                    row.append($('<td></td>').text(currency));
                    row.append($('<td></td>').text(exchangeRate));
                    row.append($('<td></td>').html(
                        '<button class="btn btn-secondary btn-sm exchange-rate-edit" data-bs-toggle="modal" data-bs-target="#edit-exchange-rate-modal">Edit</button>'
                    ));
                    tbody.append(row);
                });
            },
            error: function (jqXHR) {
                try {
                    const error = JSON.parse(jqXHR.responseText);
                    const toast = $('#api-error-toast');
                    $(toast).find('.toast-body').text(error.message || 'Request failed');
                    $(toast).toast('show');
                } catch (e) {
                    console.error('Request failed', jqXHR);
                }
            }
        });
    }

    requestExchangeRates();

    $("#add-exchange-rate").submit(function (e) {
        e.preventDefault();

        $.ajax({
            url: `${host}/exchangeRates`,
            type: "POST",
            data: $("#add-exchange-rate").serialize(),
            success: function () {
                requestExchangeRates();
            },
            error: function (jqXHR) {
                try {
                    const error = JSON.parse(jqXHR.responseText);
                    const toast = $('#api-error-toast');
                    $(toast).find('.toast-body').text(error.message || 'Request failed');
                    $(toast).toast('show');
                } catch (e) {
                    console.error('Request failed', jqXHR);
                }
            }
        });

        return false;
    });

    // Currency conversion form submission handler
    $("#convert").submit(function (e) {
        e.preventDefault();

        const amount = parseFloat($("#convert-amount").val());
        const baseCurrencyCode = $("#convert-base-currency").val();
        const targetCurrencyCode = $("#convert-target-currency").val();

        if (isNaN(amount)) {
            alert("Please enter a valid number for amount.");
            return false;
        }

        if (baseCurrencyCode === targetCurrencyCode) {
            $("#convert-converted-amount").val(amount.toFixed(2));
            return false;
        }

        $.ajax({
            url: `${host}/exchangeRate`,
            type: "GET",
            dataType: "json",
            data: {
                baseCurrencyCode: baseCurrencyCode,
                targetCurrencyCode: targetCurrencyCode
            },
            success: function (response) {
                const rate = response.rate;
                const convertedAmount = amount * rate;
                $("#convert-converted-amount").val(convertedAmount.toFixed(2));
            },
            error: function (jqXHR) {
                try {
                    const error = JSON.parse(jqXHR.responseText);
                    const toast = $('#api-error-toast');
                    $(toast).find('.toast-body').text(error.message || 'Request failed');
                    $(toast).toast('show');
                } catch (e) {
                    console.error('Request failed', jqXHR);
                }
            }
        });

        return false;
    });
});