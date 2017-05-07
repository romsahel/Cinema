set lang=%1
set dir=%2

Pushd %dir%

subliminal download -l %lang% .

popd

exit